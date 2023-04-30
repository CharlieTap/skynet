package com.tap.skynet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

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

internal fun stdout() : FlowCollector<String> {
    return FlowCollector { output ->
       println(output)
    }
}

internal suspend fun read() = withContext(Dispatchers.Default) {
    readlnOrNull() ?: throw IllegalStateException()
}

internal suspend fun write(output: String) = withContext(Dispatchers.Default) {
    println(output)
}

internal suspend fun <T: MessageBody> readMessage(json: Json,serializer: KSerializer<T>) : Message<T> {
    val serialized = read()
    return json.decodeFromString(MessageSerializer(serializer), serialized)
}

internal suspend fun <T: MessageBody> writeMessage(json: Json, deserialized: Message<T>, serializer: KSerializer<T>) {
    val output = json.encodeToString(MessageSerializer(serializer), deserialized)
    write(output)
}
