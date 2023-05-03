package com.tap.skynet.ext

import platform.posix.fflush
import platform.posix.fputs
import platform.posix.stderr

actual fun printerr(error: String) {
    fputs(error + "\n", stderr)
    fflush(stderr)
}