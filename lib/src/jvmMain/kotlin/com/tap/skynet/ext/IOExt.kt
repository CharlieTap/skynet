@file:JvmName("IOExtJvm")
package com.tap.skynet.ext

internal actual fun printerr(error: String) {
    System.err.println(error)
}