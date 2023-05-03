package com.tap.skynet.gossip

import com.tap.skynet.Node
import com.tap.skynet.actor.GetMessages
import com.tap.skynet.actor.LibraryAction
import com.tap.skynet.message.Message
import com.tap.skynet.message.MessageBody
import com.tap.skynet.message.MessageSerializer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

internal fun gossipScheduler(node: Node, messageQueue: SendChannel<LibraryAction>, serializer: Json) : Flow<String> = flow {
    while (true) {
        node.neighbours.value.forEach { nodeId ->
            val callback = CompletableDeferred<Set<Int>>()
            messageQueue.send(GetMessages(nodeId, callback))
            val messages = callback.await()

            if(messages.isNotEmpty()) {
                val gossip = Gossip(node.newMessageId(), callback.await(), node.neighbours.value)
                val message = Message(node.id, nodeId, gossip)
                val output = serializer.encodeToString(MessageSerializer(MessageBody.serializer()), message as Message<MessageBody>)
                emit(output)
            }
        }
        delay(1)
    }
}.buffer(100, BufferOverflow.DROP_OLDEST)