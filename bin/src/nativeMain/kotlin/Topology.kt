import com.tap.skynet.MessageHandler
import com.tap.skynet.NodeMessageHandler
import com.tap.skynet.Reply
import com.tap.skynet.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun topologyMessageHandler() : NodeMessageHandler<Topology, TopologyOk> = NodeMessageHandler { ctx ->
    MessageHandler { message ->
        TopologyOk(ctx.messageId(), message.body.msgId)
    }
}

@SerialName("topology")
@Serializable
data class Topology(
    @SerialName("msg_id")
    override val msgId: Int,
    val topology: Map<String, List<String>>,
): Request()

@Serializable
data class TopologyOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
): Reply.Success("topology_ok")