package com.tap.skynet.handler

import com.tap.skynet.message.Message
import com.tap.skynet.message.Response

fun interface ResponseHandler<R: Response>: suspend (Message<R>) -> Unit
