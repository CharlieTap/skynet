package com.tap.skynet.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable
data class Message<T: MessageBody>(
    val src: String,
    @SerialName("dst")
    val dst: String,
    val body: T,
)

interface IdentifiableMessage {
    @SerialName("msg_id")
    val msgId: Int
}

interface ResponseMessage {
    @SerialName("in_reply_to")
    val inReplyTo: Int
}

@Serializable
sealed interface MessageBody

/**
 * This is a bit of a hack, we depend on the fact that default classDiscriminator for polymorphic classes
 * in Kotlin serialization is type, and thus we don't have to define type. This does mean however users of the lib
 * have to declare a @SerialName annotation with the type on the concrete Request
 */
@Serializable
abstract class Request: MessageBody, IdentifiableMessage

@Serializable
sealed interface Response: MessageBody, ResponseMessage {
    @Serializable
    abstract class Success: Response, IdentifiableMessage

    @Serializable
    sealed class Error: Response
}

@SerialName("init")
@Serializable
data class Init(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("node_ids")
    val nodeIds: Set<String>
): Request()

@SerialName("init_ok")
@Serializable
data class InitOk(
    @SerialName("msg_id")
    override val msgId: Int,
    @SerialName("in_reply_to")
    override val inReplyTo: Int,
): Response.Success()

internal class MessageSerializer<T: MessageBody>(
    private val innerSerializer: KSerializer<T>
) : KSerializer<Message<T>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("message") {
        element("src", serialDescriptor<String>())
        element("dest", serialDescriptor<String>())
        element("body", innerSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Message<T>) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.src)
        encodeStringElement(descriptor, 1, value.dst)
        encodeSerializableElement(descriptor, 2, innerSerializer, value.body)
    }

    override fun deserialize(decoder: Decoder): Message<T> = decoder.decodeStructure(descriptor) {
        var src = ""
        var dst = ""
        var body: T? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> src = decodeStringElement(descriptor, 0)
                1 -> dst = decodeStringElement(descriptor, 1)
                2 -> body = decodeSerializableElement(descriptor, 1, innerSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        Message(src, dst, body!!)
    }
}