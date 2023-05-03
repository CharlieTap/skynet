package com.tap.skynet.actor

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed interface LibraryAction
data class GetMessages(val node: String = "", val callback: CompletableDeferred<Set<Int>>): LibraryAction
data class AddMessage(val message: Int): LibraryAction
data class RecordMessagesFromNode(val nodes: Set<String>, val messages: Set<Int>): LibraryAction

/**
 * Library is an actor containing a record of all messages the node has received alongside
 * a record of each of its neighbours and what messages they have seen
 */
fun CoroutineScope.library(channel : ReceiveChannel<LibraryAction>) = launch {
    // master set
    val messages : MutableSet<Int> = mutableSetOf()
    // A map containing a set of seen message for each of a neighbours
    val record : MutableMap<String, MutableSet<Int>> = mutableMapOf()
    // A set containing messages that have been gossiped but have not been ack'd
    val inflight = NetLog()

    for (message in channel) {
        ensureActive()
        when (message) {
            is AddMessage -> messages.add(message.message)
            is GetMessages -> {
                if(message.node.isEmpty()) {
                    message.callback.complete(messages.toSet())
                } else {
                    val seen = record[message.node] ?: emptySet()
                    val pending = inflight.pending(message.node)
                    val sending = messages - (seen + pending)

                    inflight.record(message.node, sending)

                    message.callback.complete(sending)
                }
            }
            is RecordMessagesFromNode -> {
                message.nodes.forEach { nodeId ->
                    if(!record.containsKey(nodeId)) {
                        record[nodeId] = message.messages.toMutableSet()
                    } else {
                        record[nodeId]?.addAll(message.messages)
                    }
                    inflight.ack(nodeId, message.messages)
                }
                messages.addAll(message.messages)
            }
        }
    }
}

private class NetLog(
    private val netlog: MutableMap<String, MutableSet<Pair<Int, Instant>>> = mutableMapOf(),
    private val timeout: Int = ESTIMATED_ROUND_TRIP_LATENCY_MILLIS + 50
) {
    companion object {
        private const val ESTIMATED_ROUND_TRIP_LATENCY_MILLIS = 200
    }

    fun pending(nodeId: String): Set<Int> {
        val pending = netlog[nodeId] ?: mutableSetOf()
        val now = Clock.System.now()
        pending.removeAll { pair ->
            now.minus(pair.second) > timeout.milliseconds
        }

        return pending.map(Pair<Int, Instant>::first).toSet()
    }

    fun record(nodeId: String, messages: Set<Int>) {
        val pending = netlog[nodeId] ?: mutableSetOf()
        val now = Clock.System.now()

        pending.addAll(messages.map { message -> message to now })
        netlog[nodeId] = pending
    }

    fun ack(nodeId: String, messages: Set<Int>) {
        val pending = netlog[nodeId] ?: mutableSetOf()
        pending.removeAll { pair ->
            messages.contains(pair.first)
        }
        netlog[nodeId] = pending
    }

}
