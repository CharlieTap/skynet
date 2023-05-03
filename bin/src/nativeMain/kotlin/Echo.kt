import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.message.Response
import com.tap.skynet.message.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun echoMessageHandler() : NodeRequestResponseHandler<Echo, EchoOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->
        EchoOk(ctx.newMessageId(), message.body.msgId, message.body.echo)
    }
}

@SerialName("echo")
@Serializable
data class Echo(
    @SerialName("msg_id")
    override val msgId: Int,
    val echo: String,
): Request()

@SerialName("echo_ok")
@Serializable
data class EchoOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    val echo: String,
): Response.Success()