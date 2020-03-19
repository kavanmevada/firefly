import kotlinx.cinterop.*
import platform.posix.*

data class HttpCall internal constructor(val socketFD: Int) {
    private enum class Type { STATUS, HEADER, BODY }

    fun request(chunk: Int = 4048, block: ((ByteArray) -> Unit)? = null): HttpRequest {
        var type = Type.STATUS
        var contentLength = 0L

        var spaceCount = 0
        var method = ""
        var url = ""
        var protocol = ""
        var key = ""
        var value = ""

        var isKey = true

        val headers = hashMapOf<String, String>()

        var endPoint = 0
        loop@ do {

            val buffer = ByteArray(chunk)

            // TODO Platform Specific
            val length = read(socketFD, buffer.refTo(0), chunk.toULong()).toInt()

            if (type == Type.BODY) {

                block?.invoke(buffer)
                contentLength -= length

                if (contentLength <= 0) break@loop
            } else {

                for (i in 0 until length) {
                    val char = buffer[i].toChar()

                    if (type == Type.STATUS) {

                        // Parse STATUS
                        when (char) {
                            '\n' -> type = Type.HEADER
                            ' ' -> spaceCount++
                            else -> when {
                                spaceCount == 0 -> method += char // Request
                                spaceCount == 1 -> url += char // Request
                                spaceCount > 1 -> protocol += char
                            }
                        }
                    } else when {
                        // Parse HEADER
                        char == '\n' -> {
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                if (block != null && contentLength <= 0 && key == "Content-Length") {
                                    contentLength = value.toLong()
                                }
                                headers[key] = value
                            }
                            key = "" // reset
                            isKey = true
                        }

                        char == ':' && isKey -> {
                            isKey = false
                            value = "" // reset
                        }

                        char != '\r' -> when {
                            isKey -> key += char
                            value.isEmpty() && char == ' ' -> {
                            }
                            else -> value += char
                        }
                    }

                    if (char == '\r' || char == '\n') endPoint++ else endPoint = 0

                    if (endPoint > 3) {

                        if (contentLength > 0) {

                            val tmpBuff = ByteArray(chunk)

                            buffer.copyInto(tmpBuff, 0, i + 1, buffer.size)

                            contentLength -= (chunk - (i + 1L))

                            if (contentLength > 0) {
                                // TODO Platform Specific
                                contentLength -= read(socketFD, tmpBuff.refTo(chunk - (i + 1)), (i + 1L).toULong())
                            }

                            block?.invoke(tmpBuff)
                        }

                        if (contentLength <= 0) break@loop
                        else type = Type.BODY; break
                    }
                }
            }
        } while (length > 0)

        return HttpRequest(method, HttpURL.parse("http://" + headers["Host"] + url), protocol, headers)
    }

    // TODO cintropless implementation
    inner class reacieveAll {
        var body = ""
        var requestheader = request { array ->
            body += array.toKString()
        }
    }
}
