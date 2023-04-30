import com.tap.skynet.MessageHandler
import com.tap.skynet.NodeMessageHandler
import com.tap.skynet.Reply
import com.tap.skynet.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun echoMessageHandler() : NodeMessageHandler<Echo, EchoOk> = NodeMessageHandler { ctx ->
    MessageHandler { message ->
        EchoOk(ctx.messageId(), message.body.msgId, message.body.echo)
    }
}

@SerialName("echo")
@Serializable
data class Echo(
    @SerialName("msg_id")
    override val msgId: Int,
    val echo: String,
): Request()

@Serializable
data class EchoOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    val echo: String,
): Reply.Success("echo_ok")