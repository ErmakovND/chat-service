package nd.ermakov.pdris.chatservice

import java.io.File

fun main(args: Array<String>) {
    val config = Configuration.fromJSON(File(args[0]))
    val server = Server(config).apply { start() }
    while (!readln().startsWith("quit")) {}
    server.stop()
}
