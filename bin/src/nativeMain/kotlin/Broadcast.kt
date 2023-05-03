
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun broadcastMessageHandler() : NodeRequestResponseHandler<Broadcast, BroadcastOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->
        ctx.storeMessage(message.body.message)
        BroadcastOk(ctx.newMessageId(), message.body.msgId)
    }
}

@SerialName("broadcast")
@Serializable
data class Broadcast(
    @SerialName("msg_id")
    override val msgId: Int,
    val message: Int,
): Request()

@SerialName("broadcast_ok")
@Serializable
data class BroadcastOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
): Response.Success()
