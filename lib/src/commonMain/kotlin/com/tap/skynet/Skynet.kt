@file:OptIn(InternalSerializationApi::class)

package com.tap.skynet

import kotlin.reflect.KClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

class Skynet internal constructor(
    private val handlerLookup: Map<KClass<Request>, Pair<NodeMessageHandler<Request, Reply>, KSerializer<Reply>>>,
    private val serializersModule: SerializersModule,
) {

    private suspend fun initialise(json: Json) : Node {
        val init = readMessage(json, Init.serializer())

        val payload = InitOk(0, init.body.msgId)
        val initOk = Message(init.dst, init.src, payload)
        writeMessage(json, initOk, InitOk.serializer())

        return Node.factory(init.body)
    }

    private fun <I: Request> runner(
        handler: MessageHandler<I,Reply>
    ) : MessageHandler<I,Reply> = MessageHandler { message ->
        kotlin.runCatching {
            handler(message)
        }.getOrElse {
            NodeError(
                ErrorCode.Crash,
                "internal-error",
                message.body.msgId
            )
        }
    }

    suspend fun run() {

        val stdin = stdin()
        val stdout = stdout()

        val serializer = serializer(serializersModule)
        val node = initialise(serializer)

        val ctx = object : NodeContext {

            override fun messageId(): Int {
                return node.messageCount.value
            }

            override fun putMeta(value: Int) {
                node.meta.push(value)
            }

            override fun meta(): List<Int> {
                return node.meta.toList()
            }


        }

        stdin.flatMapMerge { message ->
            flow {
                val input = serializer.decodeFromString(MessageSerializer(Request.serializer()), message)
                val (handler, replySerializer) = handlerLookup[input.body::class]!!
                val contextHandler = runner(handler(ctx)) // todo do this only once
                val reply = Message(input.dst, input.src, contextHandler(input))
                node.messageCount.getAndIncrement()
                val output = when(reply.body) {
                    is Reply.Error -> {
                        serializer.encodeToString(MessageSerializer(NodeError.serializer() as KSerializer<Reply>), reply)
                    }
                    is Reply.Success -> {
                        serializer.encodeToString(MessageSerializer(replySerializer), reply)
                    }
                }
                emit(output)
            }
        }.flowOn(Dispatchers.Default).collect(stdout)
    }



    class Builder {

        data class HandlerData(
            val requestClass: KClass<Request>,
            val replyClass: KClass<Reply>,
            val handler: NodeMessageHandler<Request, Reply>
        )

        @PublishedApi
        internal var handlers: MutableSet<HandlerData> = mutableSetOf()

        inline fun <reified I: Request, reified O: Reply> registerHandler(
            handler: NodeMessageHandler<I, O>
        ) {
            @Suppress("UNCHECKED_CAST")
            val data = HandlerData(
                I::class as KClass<Request>,
                O::class as KClass<Reply>,
                handler as NodeMessageHandler<Request, Reply>
            )

            handlers.add(data)
        }

        fun build(): Skynet {

            val module = SerializersModule {
                handlers.forEach { entry ->
                    polymorphic(Request::class, entry.requestClass, entry.requestClass.serializer())
                }
            }

            val handlerLookup: MutableMap<KClass<Request>, Pair<NodeMessageHandler<Request, Reply>, KSerializer<Reply>>> = mutableMapOf()
            handlers.forEach { data ->
                handlerLookup.put(data.requestClass, data.handler to data.replyClass.serializer())
            }

            return Skynet(
                handlerLookup,
                module
            )
        }
    }
}



