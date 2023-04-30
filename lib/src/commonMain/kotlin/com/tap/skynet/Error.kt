package com.tap.skynet

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

@Serializable
internal data class NodeError(
    @Serializable(with = ErrorCodeSerializer::class)
    val code: ErrorCode,
    val text: String,
    @SerialName("in_reply_to")
    override val inReplyTo: Int
): Reply.Error()

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