package com.tap.skynet.gossip

import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.NodeResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.handler.ResponseHandler
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal fun gossipMessageHandler(): NodeRequestResponseHandler<Gossip, GossipOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->
        ctx.recordNeighbourMessages(message.body.recipients + message.src , message.body.messages)
        GossipOk(ctx.newMessageId(), message.body.msgId, message.body.messages)
    }
}

internal fun gossipResponseHandler(): NodeResponseHandler<GossipOk> = NodeResponseHandler { ctx ->
    ResponseHandler { message ->
        ctx.recordNeighbourMessages(setOf(message.src), message.body.messages)
    }
}

@SerialName("gossip")
@Serializable
internal data class Gossip(
    @SerialName("msg_id")
    override val msgId: Int,
    val messages: Set<Int>,
    val recipients: Set<String>
): Request()

@SerialName("gossip_ok")
@Serializable
internal data class GossipOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    val messages: Set<Int>,
): Response.Success()
