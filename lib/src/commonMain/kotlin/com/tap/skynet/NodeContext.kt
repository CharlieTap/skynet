package com.tap.skynet

interface NodeContext {
    fun nodeId(): String
    fun newMessageId(): Int
    fun setNeighbours(neighbours: Set<String>)
    suspend fun storeMessage(value: Int)
    suspend fun recordNeighbourMessages(node: String, messages: Set<Int>)
    suspend fun messages(): Set<Int>
}