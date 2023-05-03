package com.tap.skynet.handler

import com.tap.skynet.NodeContext
import com.tap.skynet.message.Request
import com.tap.skynet.message.Response

fun interface NodeRequestResponseHandler<I: Request, O: Response>:(NodeContext) -> RequestResponseHandler<I, O>
