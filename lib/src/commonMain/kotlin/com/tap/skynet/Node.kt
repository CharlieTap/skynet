package com.tap.skynet

import com.tap.skynet.actor.AddMessage
import com.tap.skynet.actor.GetMessages
import com.tap.skynet.actor.LibraryAction
import com.tap.skynet.actor.RecordMessagesFromNode
import com.tap.skynet.message.Init
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

internal data class Node(
    val id: String,
    val nodesInCluster: Set<String>,
    val neighbours: AtomicRef<Set<String>>,
    val messageCount: AtomicInt,
    val messageQueue: SendChannel<LibraryAction>
): NodeContext {

    companion object {
        fun factory(msg: Init, messageQueue: SendChannel<LibraryAction>) : Node {
            return Node(
                msg.nodeId,
                msg.nodeIds,
                atomic(emptySet()),
                atomic(1),
                messageQueue
            )
        }
    }

    override fun nodeId(): String {
       return id
    }

    override fun newMessageId(): Int {
        return messageCount.getAndIncrement()
    }

    override fun nodesInCluster(): Set<String> {
        return nodesInCluster
    }

    override fun setNeighbours(neighbours: Set<String>) {
         this.neighbours.update {
             neighbours
         }
    }

    override suspend fun storeMessage(value: Int) {
        messageQueue.send(AddMessage(value))
    }

    override suspend fun recordNeighbourMessages(nodes: Set<String>, messages: Set<Int>) {
        messageQueue.send(RecordMessagesFromNode(nodes, messages))
    }

    override suspend fun messages(): Set<Int> {
        val callback = CompletableDeferred<Set<Int>>()
        val message = GetMessages(callback = callback)
        messageQueue.send(message)
        return callback.await()
    }

}
