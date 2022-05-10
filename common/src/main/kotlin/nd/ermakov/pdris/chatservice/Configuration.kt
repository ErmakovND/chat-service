package nd.ermakov.pdris.chatservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class Configuration(
    val port: Int
) {
    companion object
}

fun Configuration.Companion.fromJSON(f: File) =
    jacksonObjectMapper().readValue<Configuration>(f)
