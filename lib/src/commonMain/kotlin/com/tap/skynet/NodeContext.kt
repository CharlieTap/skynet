package com.tap.skynet

interface NodeContext {
    fun messageId(): Int
    fun putMeta(value: Int)
    fun meta(): List<Int>
}