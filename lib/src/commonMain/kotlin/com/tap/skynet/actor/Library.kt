package com.tap.skynet.actor

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

sealed interface LibraryAction
data class GetMessages(val node: String = "", val callback: CompletableDeferred<Set<Int>>): LibraryAction
data class AddMessage(val message: Int): LibraryAction
data class RecordMessagesFromNode(val node: String, val messages: Set<Int>): LibraryAction

/**
 * Library is an actor containing a record of all messages the node has received alongside
 * a record of each of its neighbours and what messages they have seen
 */
fun CoroutineScope.library(channel : ReceiveChannel<LibraryAction>) = launch {
    // master set
    val messages : MutableSet<Int> = mutableSetOf()
    // A map containing a set of seen message for each of a neighbours
    val record : MutableMap<String, MutableSet<Int>> = mutableMapOf()

    for (message in channel) {
        ensureActive()
        when (message) {
            is AddMessage -> messages.add(message.message)
            is GetMessages -> {
                if(message.node.isEmpty()) {
                    message.callback.complete(messages.toSet())
                } else {
                    val seen = record[message.node] ?: emptySet()
                    message.callback.complete(messages - seen)
                }
            }
            is RecordMessagesFromNode -> {
                if(!record.containsKey(message.node)) {
                    record[message.node] = message.messages.toMutableSet()
                } else {
                    record[message.node]?.addAll(message.messages)
                }
                messages.addAll(message.messages)
            }
        }
    }
}
