package com.tap.skynet

interface NodeContext {
    fun nodeId(): String
    fun newMessageId(): Int
    fun nodesInCluster(): Set<String>
    fun setNeighbours(neighbours: Set<String>)
    suspend fun storeMessage(value: Int)
    suspend fun recordNeighbourMessages(nodes: Set<String>, messages: Set<Int>)
    suspend fun messages(): Set<Int>
}