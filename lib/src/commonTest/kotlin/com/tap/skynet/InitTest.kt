package com.tap.skynet

import app.cash.turbine.test
import com.tap.skynet.message.Init
import com.tap.skynet.message.InitOk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule


class InitTest: SkynetTestClass() {

    override val testModule: SerializersModule = EmptySerializersModule()

    @Test
    fun `ingestion of init is met with a corresponding init_ok message`() = scope.runTest {
        val skynet = builder.build().run()

        val init = inbound(Init(0, "n0", setOf("n1", "n2")))
        val initOk = outbound(InitOk(0, 0))
        val expected = serializeMessage(initOk)


        stdout.test {
            stdin.emit(serializeMessage(init))
            val event = awaitItem()
            assertEquals(expected, event)
        }
    }

}