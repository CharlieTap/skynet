package com.tap.skynet.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
internal enum class ErrorCode(val code: Int) {
    Timeout(0),
    NodeNotFound(1),
    NotSupported(10),
    TemporarilyUnavailable(11),
    MalformedRequest(12),
    Crash(13),
    Abort(14),
    KeyDoesNotExist(20),
    KeyAlreadyExists(21),
    PreconditionFailed(22),
    TransactionConflict(30),
}

@SerialName("error")
@Serializable
internal data class NodeError(
    @Serializable(with = ErrorCodeSerializer::class)
    val code: ErrorCode,
    val text: String,
    @SerialName("in_reply_to")
    override val inReplyTo: Int
): Response.Error() {
    companion object {
        fun crash(error: String, messageId: Int = 0) : NodeError {
            return NodeError(
                ErrorCode.Crash,
                error,
                messageId
            )
        }
    }
}

@Serializer(forClass = ErrorCode::class)
internal object ErrorCodeSerializer : KSerializer<ErrorCode> {

    override val descriptor: SerialDescriptor = serialDescriptor<Int>()

    override fun serialize(encoder: Encoder, value: ErrorCode) = encoder.encodeInt(value.code)

    override fun deserialize(decoder: Decoder): ErrorCode = decoder.decodeInt().let { code ->
        ErrorCode.values().first {
            it.code == code
        }
    }

}