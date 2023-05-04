package com.tap.skynet

import com.tap.skynet.ext.serializer
import com.tap.skynet.message.Init
import com.tap.skynet.message.InitOk
import com.tap.skynet.message.Message
import com.tap.skynet.message.MessageBody
import com.tap.skynet.message.MessageSerializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

abstract class SkynetTestClass {

    val builder: Skynet.Builder = Skynet.Builder()
    val stdin : MutableSharedFlow<String> = MutableSharedFlow(10)
    val stdout : MutableSharedFlow<String> = MutableSharedFlow(10)
    val stderr : MutableSharedFlow<String> = MutableSharedFlow(10)
    val scope = TestScope()

    private val internalModule = SerializersModule {
        polymorphic(MessageBody::class, Init::class, Init.serializer())
        polymorphic(MessageBody::class, InitOk::class, InitOk.serializer())
    }

    abstract val testModule: SerializersModule

    private val serializer: Json
        get() = serializer(internalModule + testModule)

    @BeforeTest
    fun setup() {
        builder.apply {
            scope(scope.backgroundScope)
            input(stdin)
            output {
                stdout.emit(it)
            }
            error {
                stderr.emit(it)
            }
        }
    }

    @AfterTest
    fun cleanup() {
        stdin.resetReplayCache()
        stdout.resetReplayCache()
        stderr.resetReplayCache()
    }

    fun <T: MessageBody> serializeMessage(message: Message<T>): String {
        return serializer.encodeToString(MessageSerializer(MessageBody.serializer()), message as Message<MessageBody>)
    }

    suspend fun init() {
        val init = inbound(Init(0, "n0", setOf("n1", "n2")))
        stdin.emit(serializeMessage(init))
    }

    private fun <T: MessageBody> hoistBody(body: T, inbound: Boolean): Message<T> {
        val src = if(inbound) {
            "c0"
        } else "n0"
        val dst = if(inbound) {
            "n0"
        } else "c0"
        return Message(src, dst, body)
    }

    fun <T: MessageBody> inbound(body: T): Message<T> {
        return hoistBody(body, true)
    }

    fun <T: MessageBody>outbound(body: T): Message<T> {
        return hoistBody(body, false)
    }

}