package nd.ermakov.pdris.chatservice

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class Message(
    val text: String,
    val fileName: String = "",
    val fileData: ByteBuffer = ByteBuffer.allocate(0),
    val connection: Boolean = false
)

private fun ByteBuffer.getString() = ByteArray(remaining()).let {
    get(it)
    String(it)
}

private fun String.toByteBuffer() = ByteBuffer.wrap(toByteArray())

private fun Boolean.toByteBuffer() = ByteBuffer.wrap(byteArrayOf(if (this) 1 else 0))

fun ByteChannel.readMessage(): Message? {
    val text = readBySize() ?: return null
    val filename = readBySize() ?: return null
    val filedata = readBySize() ?: return null
    val connection = read(1) ?: return null
    return Message(
        text.getString(),
        filename.getString(),
        filedata,
        connection.get() == 1.toByte()
    )
}

fun ByteChannel.readBySize(): ByteBuffer? {
    val size = read(4) ?: return null
    return read(size.int)
}

fun ByteChannel.read(n: Int): ByteBuffer? {
    val b = ByteBuffer.allocate(n)
    while (b.hasRemaining()) {
        if (read(b) == -1) {
            return null
        }
    }
    return b.rewind()
}

fun ByteChannel.sendMessage(msg: Message) {
    sendWithSize(msg.text.toByteBuffer())
    sendWithSize(msg.fileName.toByteBuffer())
    sendWithSize(msg.fileData)
    send(msg.connection.toByteBuffer())
}

fun ByteChannel.sendWithSize(b: ByteBuffer) {
    val sb = ByteBuffer.allocate(Int.SIZE_BYTES).apply {
        putInt(b.limit())
        rewind()
    }
    send(sb)
    send(b)
}

fun ByteChannel.send(b: ByteBuffer) {
    while (b.hasRemaining()) {
        write(b)
    }
    b.rewind()
}
