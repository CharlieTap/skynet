package com.tap.skynet

import app.cash.turbine.test
import com.tap.skynet.gossip.Gossip
import com.tap.skynet.gossip.GossipOk
import com.tap.skynet.message.MessageBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.modules.SerializersModule


class GossipTest: SkynetTestClass() {

    override val testModule: SerializersModule = SerializersModule {
        polymorphic(MessageBody::class, Gossip::class, Gossip.serializer())
        polymorphic(MessageBody::class, GossipOk::class, GossipOk.serializer())
    }

    @Test
    fun `ingestion of gossip is met with a corresponding gossip_ok message`() = scope.runTest {

        val skynet = builder.build().run()
        init()

        val messages = setOf(1,2,3)
        val gossip = inbound(Gossip(1, messages, setOf("n0")))
        val gossipOk = outbound(GossipOk(1, 1, messages))
        val expected = serializeMessage(gossipOk)

        stdout.drop(1).test {
            stdin.emit(serializeMessage(gossip))
            val event = awaitItem()
            assertEquals(expected, event)
        }
    }

}