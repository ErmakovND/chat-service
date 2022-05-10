package nd.ermakov.pdris.chatservice

import java.io.File
import java.io.FileNotFoundException

const val CONNECT = "connect"
const val TEXT = "text"
const val FILE = "file"
const val QUIT = "quit"

fun main(args: Array<String>) {
    val config = Configuration.fromJSON(File(args[0]))
    val client = Client(config)
    val messageHandler: (Message) -> Unit = {
        if (it.fileData.hasRemaining()) {
            File(it.fileName).outputStream().channel.send(it.fileData)
        }
        println(it.text)
    }
    while (true) {
        val s = readln().split(" ", limit = 2)
        val cmd = s.getOrElse(0) { "" }
        val arg = s.getOrElse(1) { "" }
        when (cmd) {
            CONNECT -> {
                client.connect(arg, messageHandler)
            }
            TEXT -> {
                client.sendText(arg)
            }
            FILE -> {
                val name = arg.substringBefore(" ")
                val path = arg.substringAfter(" ")
                try {
                    client.sendFile(name, File(path))
                } catch (e: FileNotFoundException) {
                    println("(Failed to send file: ${e.message})")
                }
            }
            QUIT -> {
                client.quit()
                break
            }
        }
    }
}
