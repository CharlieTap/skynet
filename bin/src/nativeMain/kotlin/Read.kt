
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun readMessageHandler() : NodeRequestResponseHandler<Read, ReadOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->
        ReadOk(ctx.newMessageId(), message.body.msgId, ctx.messages())
    }
}

@SerialName("read")
@Serializable
data class Read(
    @SerialName("msg_id")
    override val msgId: Int,
): Request()

@SerialName("read_ok")
@Serializable
data class ReadOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    val messages: Set<Int>,
): Response.Success()