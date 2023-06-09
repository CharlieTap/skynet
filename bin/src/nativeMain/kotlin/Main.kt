
import com.tap.skynet.Skynet
import com.tap.skynet.ext.printerr
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import platform.posix.SIGINT
import platform.posix.SIGTERM
import platform.posix.signal

val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

fun main() {
    setupSignalHandler()
    val skynet = Skynet.Builder().apply {
        scope(applicationScope)
        registerHandler(echoMessageHandler())
        registerHandler(generateMessageHandler())
        registerHandler(broadcastMessageHandler())
        registerHandler(readMessageHandler())
        registerHandler(topologyMessageHandler())
    }.build()
    runBlocking {
        skynet.run().join()
    }
}

fun setupSignalHandler() {
    val handler = staticCFunction { _: Int ->
        printerr("Gracefully shutting down")
        applicationScope.cancel("Signal received. Cancelling the application scope.")
    }

    signal(SIGTERM, handler)
    signal(SIGINT, handler)
}
