package com.tap.skynet.ext

import com.tap.skynet.message.Message
import com.tap.skynet.message.MessageBody
import com.tap.skynet.message.MessageSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal expect fun printerr(error: String)

internal fun stdin() : Flow<String> {
     return flow {
         do {
             val line = readlnOrNull()
             line?.let { serialized ->
                emit(serialized)
             }
         } while (line != null)
     }
     .buffer(5000)
     .flowOn(Dispatchers.IO)
 }

internal object stdout: FlowCollector<String> {
    private val lock: Mutex = Mutex()
    override suspend fun emit(value: String) {
        lock.withLock {
            println(value)
        }
    }
}

internal object stderr: FlowCollector<String> {
    private val lock: Mutex = Mutex()
    override suspend fun emit(value: String) {
        lock.withLock {
            printerr(value)
        }
    }
}

internal suspend fun read() = withContext(Dispatchers.IO) {
    readlnOrNull() ?: throw IllegalStateException()
}

internal suspend fun write(output: String) = withContext(Dispatchers.IO) {
    stdout.emit(output)
}

internal suspend fun writeErr(output: String) = withContext(Dispatchers.IO) {
    stderr.emit(output)
}

internal suspend fun <T: MessageBody> readMessage(json: Json, serializer: KSerializer<T>) : Message<T> {
    val serialized = read()
    return json.decodeFromString(MessageSerializer(serializer), serialized)
}

internal suspend fun <T: MessageBody> writeMessage(json: Json, deserialized: Message<T>, serializer: KSerializer<T>) {
    val output = json.encodeToString(MessageSerializer(serializer), deserialized)
    write(output)
}

internal suspend fun <T: MessageBody> writeErrMessage(json: Json, deserialized: Message<T>, serializer: KSerializer<T>) {
    val output = json.encodeToString(MessageSerializer(serializer), deserialized)
    writeErr(output)
}
