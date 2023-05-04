@file:OptIn(InternalSerializationApi::class)

package com.tap.skynet

import com.tap.skynet.actor.LibraryAction
import com.tap.skynet.actor.launchLibrary
import com.tap.skynet.ext.stderr
import com.tap.skynet.ext.stdin
import com.tap.skynet.ext.stdout
import com.tap.skynet.ext.writeMessage
import com.tap.skynet.gossip.Gossip
import com.tap.skynet.gossip.GossipOk
import com.tap.skynet.gossip.gossipMessageHandler
import com.tap.skynet.gossip.gossipResponseHandler
import com.tap.skynet.gossip.gossipScheduler
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.message.Init
import com.tap.skynet.message.InitOk
import com.tap.skynet.message.Message
import com.tap.skynet.message.MessageBody
import com.tap.skynet.message.MessageSerializer
import com.tap.skynet.message.NodeError
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import com.tap.skynet.ext.serializer as JsonSerializer

class Skynet internal constructor(
    private val handlerLookup: Map<KClass<Request>, NodeRequestResponseHandler<Request, Response>>,
    private val serializer: Json,
    private val scope: CoroutineScope,
    private val input: Flow<String>,
    private val output: FlowCollector<String>,
    private val error: FlowCollector<String>,
    private val messageQueue: Channel<LibraryAction>,
) {

    companion object {

        private suspend fun init(
            input: Flow<Message<MessageBody>>,
            output: FlowCollector<String>,
            serializer: Json,
            messageQueue: SendChannel<LibraryAction>
        ) : Node {
            val init = input.first() as Message<Init>

            val payload = InitOk(0, init.body.msgId)
            val initOk = Message(init.dst, init.src, payload)
            writeMessage(output, serializer, initOk as Message<MessageBody>, MessageBody.serializer())

            return Node.factory(init.body, messageQueue)
        }

        private fun <I: Request> runner(
            handler: RequestResponseHandler<I, Response>
        ) : RequestResponseHandler<I, Response> = RequestResponseHandler { message ->
            runCatching {
                handler(message)
            }.getOrElse {
                NodeError.crash(it.toString())
            }
        }

    }

    private val messages = input.map { message ->
        serializer.decodeFromString(MessageSerializer(MessageBody.serializer()), message)
    }.shareIn(scope, SharingStarted.Lazily, 1000)

    private val requests = messages
        .filter { it.body is Request && it.body !is Init }
        .filterIsInstance<Message<Request>>()

    private val responses = messages
        .filter { it.body is Response }
        .filterIsInstance<Message<Response>>()


    @JvmName("processRequest")
    private fun Flow<Message<Request>>.process(ctx: NodeContext): Flow<Message<Response>> {
        return flatMapMerge { message ->
            flow {
                val handler = handlerLookup[message.body::class] ?: run {
                    val error = NodeError.crash("Unable to find handler for message: $message")
                    val crash = Message(message.dst, message.src, error)
                    emit(crash as Message<Response>)
                    return@flow
                }
                val contextHandler = runner(handler(ctx)) // todo do this only once
                val reply = Message(message.dst, message.src, contextHandler(message))
                emit(reply)
            }
        }.flowOn(Dispatchers.Default)
    }

    @JvmName("processResponse")
    private fun Flow<Message<Response>>.process(ctx: NodeContext): Flow<Unit> {
        return map { message ->
            val handler = gossipResponseHandler()(ctx)
            handler(message as Message<GossipOk>)
        }.flowOn(Dispatchers.Default)
    }


    private fun <T: MessageBody> Flow<Message<T>>.serialize(serializer: Json): Flow<String> {
        return map { message ->
            serializer.encodeToString(MessageSerializer(MessageBody.serializer()), message as Message<MessageBody>)
        }.flowOn(Dispatchers.Default)
    }

    private fun CoroutineScope.launchGossipScheduler(node: Node) = launch {
        gossipScheduler(node, messageQueue, serializer).collect(output)
    }

    private fun CoroutineScope.launchCallbackServer(node: Node) = launch {
        responses.process(node).collect()
    }

    suspend fun run() = scope.launch {

        val node = init(messages, output, serializer, messageQueue)

        val state = launchLibrary(messageQueue)
        val scheduler = launchGossipScheduler(node)
        val callbacks = launchCallbackServer(node)

        val processed = requests.process(node).shareIn(scope, SharingStarted.Lazily, 100)

        val successes = launch {
            processed.filter { it.body is Response.Success }.serialize(serializer).collect(output)
        }

        val failures = launch {
            processed.filter { it.body is Response.Error }.serialize(serializer).collect(error)
        }

        listOf(state, scheduler, callbacks, successes, failures).joinAll()
    }



    class Builder {

        private var serializer: Json? = null

        fun serializer(serializer: Json) {
            this.serializer = serializer
        }

        private var input: Flow<String>? = null

        fun input(input: Flow<String>) {
            this.input = input
        }

        private var output: FlowCollector<String>? = null

        fun output(output: FlowCollector<String>) {
            this.output = output
        }

        private var error: FlowCollector<String>? = null

        fun error(error: FlowCollector<String>) {
            this.error = error
        }

        private var scope: CoroutineScope? = null

        fun scope(scope: CoroutineScope) {
            this.scope = scope
        }

        private var messageQueue: Channel<LibraryAction>? = null

        fun messageQueue(messageQueue: Channel<LibraryAction>) {
            this.messageQueue = messageQueue
        }


        data class HandlerData(
            val requestClass: KClass<Request>,
            val responseClass: KClass<Response>,
            val handler: NodeRequestResponseHandler<Request, Response>
        )

        @PublishedApi
        internal var handlers: MutableSet<HandlerData> = mutableSetOf()

        inline fun <reified I: Request, reified O: Response> registerHandler(
            handler: NodeRequestResponseHandler<I, O>
        ) {
            @Suppress("UNCHECKED_CAST")
            val data = HandlerData(
                I::class as KClass<Request>,
                O::class as KClass<Response>,
                handler as NodeRequestResponseHandler<Request, Response>
            )

            handlers.add(data)
        }

        fun build(): Skynet {

            registerHandler(gossipMessageHandler())

            val module = SerializersModule {
                polymorphic(MessageBody::class, Init::class, Init.serializer())
                polymorphic(MessageBody::class, InitOk::class, InitOk.serializer())
                polymorphic(MessageBody::class, Gossip::class, Gossip.serializer())
                polymorphic(MessageBody::class, GossipOk::class, GossipOk.serializer())
                handlers.forEach { entry ->
                    polymorphic(MessageBody::class, entry.requestClass, entry.requestClass.serializer())
                    polymorphic(MessageBody::class, entry.responseClass, entry.responseClass.serializer())
                }
            }

            val handlerLookup: MutableMap<KClass<Request>, NodeRequestResponseHandler<Request, Response>> = mutableMapOf()
            handlers.forEach { data ->
                handlerLookup[data.requestClass] = data.handler
            }

            return Skynet(
                handlerLookup = handlerLookup,
                serializer = serializer ?: JsonSerializer(module),
                input = input ?: stdin,
                output = output ?: stdout,
                error = error ?: stderr,
                scope = scope ?: CoroutineScope(Job() + Dispatchers.Default),
                messageQueue = messageQueue ?: Channel(1000),
            )
        }
    }
}



