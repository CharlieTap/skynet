
import com.tap.skynet.Skynet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() {
    val skynet = Skynet.Builder().apply {
        registerHandler(echoMessageHandler())
        registerHandler(generateMessageHandler())
        registerHandler(broadcastMessageHandler())
        registerHandler(readMessageHandler())
        registerHandler(topologyMessageHandler())
    }.build()
    runBlocking {
        withContext(Dispatchers.Default) {
            skynet.run()
        }
    }
}
