
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response
import com.tap.skynet.topology.clusterToTree
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun topologyMessageHandler() : NodeRequestResponseHandler<Topology, TopologyOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->

        val topology = clusterToTree(3, ctx.nodesInCluster())
        ctx.setNeighbours(topology[ctx.nodeId()]!!.toSet())

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