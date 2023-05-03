
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun topologyMessageHandler() : NodeRequestResponseHandler<Topology, TopologyOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->

        val allOtherNodes = ctx.nodesInCluster()
        val gossipNodeSize = ((allOtherNodes.size / 4) * 3) + 1
//        if(gossipNodeSize >= allOtherNodes.size) {
            ctx.setNeighbours(allOtherNodes)
//        } else {
//            val gossipNodes = (1..gossipNodeSize).fold(mutableSetOf<String>()) { acc, _ ->
//                while (true) {
//                    val result = acc.add(allOtherNodes.random())
//                    if(result) {
//                        break
//                    }
//                }
//                acc
//            }
//            ctx.setNeighbours(gossipNodes)
//        }

        TopologyOk(ctx.newMessageId(), message.body.msgId)
    }
}

@SerialName("topology")
@Serializable
data class Topology(
    @SerialName("msg_id")
    override val msgId: Int,
    val topology: Map<String, List<String>>,
): Request()

@SerialName("topology_ok")
@Serializable
data class TopologyOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
): Response.Success()