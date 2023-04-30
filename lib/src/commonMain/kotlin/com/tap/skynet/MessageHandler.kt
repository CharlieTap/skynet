package com.tap.skynet

fun interface MessageHandler<I: Request, O: Reply>: (Message<I>) -> O
fun interface NodeMessageHandler<I: Request, O: Reply>:(NodeContext) -> MessageHandler<I, O>
