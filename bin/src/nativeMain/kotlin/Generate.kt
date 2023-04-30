
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.tap.skynet.MessageHandler
import com.tap.skynet.NodeMessageHandler
import com.tap.skynet.Reply
import com.tap.skynet.Request
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

fun generateMessageHandler() : NodeMessageHandler<Generate, GenerateOk> = NodeMessageHandler { ctx ->
    MessageHandler { message ->
        GenerateOk(ctx.messageId(), message.body.msgId, uuid4())
    }
}

@SerialName("generate")
@Serializable
data class Generate(
    @SerialName("msg_id")
    override val msgId: Int,
): Request()

@Serializable
data class GenerateOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
): Reply.Success("generate_ok")

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