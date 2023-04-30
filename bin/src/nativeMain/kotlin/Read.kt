import com.tap.skynet.MessageHandler
import com.tap.skynet.NodeMessageHandler
import com.tap.skynet.Reply
import com.tap.skynet.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun readMessageHandler() : NodeMessageHandler<Read, ReadOk> = NodeMessageHandler { ctx ->
    MessageHandler { message ->
        ReadOk(ctx.messageId(), message.body.msgId, ctx.meta())
    }
}

@SerialName("read")
@Serializable
data class Read(
    @SerialName("msg_id")
    override val msgId: Int,
): Request()

@Serializable
data class ReadOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    val messages: List<Int>,
): Reply.Success("read_ok")