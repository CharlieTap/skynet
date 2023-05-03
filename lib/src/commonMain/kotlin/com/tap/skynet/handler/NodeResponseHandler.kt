package com.tap.skynet.handler

import com.tap.skynet.NodeContext
import com.tap.skynet.message.Response

fun interface NodeResponseHandler<R: Response>:(NodeContext) -> ResponseHandler<R>
