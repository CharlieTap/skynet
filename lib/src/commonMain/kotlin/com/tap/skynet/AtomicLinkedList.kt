package com.tap.skynet

import kotlinx.atomicfu.atomic

class AtomicLinkedList<T> {
    private val head = atomic<Node<T>?>(null)

    fun push(value: T) {
        while (true) {
            val currentHead = head.value
            val newHead = Node(value, currentHead)
            if (head.compareAndSet(currentHead, newHead)) {
                break
            }
        }
    }

    fun toList(): List<T> {
        val result = mutableListOf<T>()
        var current = head.value
        while (current != null) {
            result.add(current.value)
            current = current.next.value
        }
        return result
    }

    private class Node<T>(val value: T, next: Node<T>?) {
        val next = atomic(next)
    }
}