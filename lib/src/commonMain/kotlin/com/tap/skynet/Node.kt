package com.tap.skynet

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic

internal data class Node(
    val id: String,
    val neighbours: Set<String>,
    val messageCount: AtomicInt,
    val meta: AtomicLinkedList<Int>
) {
    companion object {
        fun factory(msg: Init) : Node {
            return Node(
                msg.nodeId,
                msg.nodeIds - msg.nodeId,
                atomic(1),
                AtomicLinkedList()
            )
        }
    }
}







