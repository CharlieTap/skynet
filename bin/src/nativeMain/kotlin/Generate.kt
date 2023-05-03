
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.tap.skynet.handler.RequestResponseHandler
import com.tap.skynet.handler.NodeRequestResponseHandler
import com.tap.skynet.message.Response
import com.tap.skynet.message.Request
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

fun generateMessageHandler() : NodeRequestResponseHandler<Generate, GenerateOk> = NodeRequestResponseHandler { ctx ->
    RequestResponseHandler { message ->
        GenerateOk(ctx.newMessageId(), message.body.msgId, uuid4())
    }
}

@SerialName("generate")
@Serializable
data class Generate(
    @SerialName("msg_id")
    override val msgId: Int,
): Request()

@SerialName("generate_ok")
@Serializable
data class GenerateOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
): Response.Success()

object UuidSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Uuid {
        val string = decoder.decodeString()
        return uuidFrom(string)
    }
}