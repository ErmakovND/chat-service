package nd.ermakov.pdris.chatservice

import java.io.File
import java.net.InetSocketAddress
import java.nio.channels.ClosedSelectorException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class Client(private val configuration: Configuration) {

    private val selector = Selector.open()
    private val socket = SocketChannel.open()
    private val thread = Thread {
        while (true) {
            try {
                selector.apply {
                    select()
                    for (key in selectedKeys()) {
                        when {
                            key.isReadable -> handleRead(key)
                        }
                    }
                }
            } catch (e: ClosedSelectorException) {
                break
            }
        }
    }
    private lateinit var name: String
    private lateinit var onMessage: (Message) -> Unit

    private fun handleRead(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val message = channel.readMessage() ?: return key.cancel()
        onMessage(message)
    }

    fun connect(name: String, onMessage: (Message) -> Unit) {
        this.name = name
        this.onMessage = onMessage
        socket.apply {
            connect(InetSocketAddress(configuration.port))
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
        thread.start()
        socket.sendMessage(Message(name, connection = true))
    }

    fun sendText(text: String) {
        socket.sendMessage(Message(text))
    }

    fun sendFile(name: String, file: File) {
        val fileData = file.inputStream().channel.read(file.length().toInt()) ?: return
        socket.sendMessage(Message(
            text = "",
            fileName = name,
            fileData = fileData
        ))
    }

    fun quit() {
        selector.close()
        socket.close()
        thread.join()
    }
}
