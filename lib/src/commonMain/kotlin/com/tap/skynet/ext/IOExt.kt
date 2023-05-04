package com.tap.skynet.ext

import com.tap.skynet.message.Message
import com.tap.skynet.message.MessageBody
import com.tap.skynet.message.MessageSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal expect fun printerr(error: String)

internal object stdin: Flow<String> {
    override suspend fun collect(collector: FlowCollector<String>) {
        flow {
            do {
                val line = readlnOrNull()
                line?.let { serialized ->
                    emit(serialized)
                }
            } while (line != null)
        }
        .buffer(5000)
        .flowOn(Dispatchers.IO)
        .collect(collector)
    }
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

internal suspend fun <T: MessageBody> readMessage(input: Flow<String>, json: Json, serializer: KSerializer<T>) : Message<T> {
    val serialized = input.first()
    return json.decodeFromString(MessageSerializer(serializer), serialized)
}

internal suspend fun <T: MessageBody> writeMessage(output: FlowCollector<String>, json: Json, deserialized: Message<T>, serializer: KSerializer<T>) {
    val serialized = json.encodeToString(MessageSerializer(serializer), deserialized)
    output.emit(serialized)
}
