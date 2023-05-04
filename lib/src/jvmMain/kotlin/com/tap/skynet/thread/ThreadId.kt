package com.tap.skynet.thread

actual fun threadId(): Long {
    return Thread.currentThread().id
}