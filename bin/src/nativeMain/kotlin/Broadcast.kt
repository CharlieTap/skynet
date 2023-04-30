import com.tap.skynet.MessageHandler
import com.tap.skynet.NodeMessageHandler
import com.tap.skynet.Reply
import com.tap.skynet.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun broadcastMessageHandler() : NodeMessageHandler<Broadcast, BroadcastOk> = NodeMessageHandler { ctx ->
    MessageHandler { message ->
        ctx.putMeta(message.body.message)
        BroadcastOk(ctx.messageId(), message.body.msgId)
    }
}

@SerialName("broadcast")
@Serializable
data class Broadcast(
    @SerialName("msg_id")
    override val msgId: Int,
    val message: Int,
): Request()

@Serializable
data class BroadcastOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
): Reply.Success("broadcast_ok")
