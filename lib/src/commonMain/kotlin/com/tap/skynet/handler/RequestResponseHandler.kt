package com.tap.skynet.handler

import com.tap.skynet.message.Message
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response

fun interface RequestResponseHandler<I: Request, O: Response>: suspend (Message<I>) -> O
