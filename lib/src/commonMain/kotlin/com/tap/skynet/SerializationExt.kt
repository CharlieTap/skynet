package com.tap.skynet

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

fun serializer(module: SerializersModule? = null) : Json = Json {
    ignoreUnknownKeys = true
    module?.let {
        serializersModule = module
    }
}