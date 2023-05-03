package com.tap.skynet.thread

import kotlinx.cinterop.toLong
import platform.posix.pthread_self

actual fun threadId(): Long = pthread_self().toLong()