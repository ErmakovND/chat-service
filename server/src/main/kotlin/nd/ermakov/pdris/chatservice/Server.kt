package nd.ermakov.pdris.chatservice

import java.net.InetSocketAddress
import java.nio.channels.ClosedSelectorException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class Server(private val configuration: Configuration) {

    private val names = ConcurrentHashMap<SelectionKey, String>()
    private val selector = Selector.open()
    private val ssc = ServerSocketChannel.open()
    private val thread = Thread {
        while (true) {
            try {
                selector.apply {
                    select()
                    for (key in selectedKeys()) {
                        when {
                            key.isAcceptable -> handleAccept()
                            key.isReadable -> handleRead(key)
                        }
                    }
                    selectedKeys().clear()
                }
            } catch (e: ClosedSelectorException) {
                break
            }
        }
    }

    fun start() {
        ssc.apply {
            bind(InetSocketAddress(configuration.port))
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
        thread.start()
    }

    fun stop() {
        selector.close()
        ssc.close()
        thread.join()
    }

    private fun handleAccept() {
        ssc.accept().apply {
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
    }

    private fun handleRead(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val message = channel.readMessage()
        when {
            message == null -> {
                broadcast(
                    Message("[${names[key]} left chat]"),
                    key
                )
                key.cancel()
            }
            message.connection -> {
                val name = message.text.also { names[key] = it }
                unicast(
                    Message("[Welcome to chat, $name!]"),
                    key
                )
                broadcast(
                    Message("[$name joined chat]"),
                    key
                )
            }
            message.text.isNotEmpty() -> {
                broadcast(
                    Message("[${names[key]}]: ${message.text}"),
                    key
                )
            }
            message.fileData.hasRemaining() -> {
                broadcast(
                    Message(
                        "[${names[key]} sent file ${message.fileName}]",
                        message.fileName,
                        message.fileData
                    ),
                    key
                )
            }
        }
    }

    private fun unicast(msg: Message, to: SelectionKey) {
        val channel = to.channel()
        if (channel is SocketChannel) {
            channel.sendMessage(msg)
        }
    }

    private fun broadcast(msg: Message, from: SelectionKey) {
        for (key in selector.keys().filter { it != from }) {
            unicast(msg, key)
        }
    }
}
