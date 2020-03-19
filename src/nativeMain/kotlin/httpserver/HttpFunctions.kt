data class HttpRequest internal constructor(
    val method: String,
    val url: HttpURL,
    val protocol: String,
    val headers: MutableMap<String, String>
)

data class HttpResponse internal constructor(
    val protocol: String,
    val statusCode: Int,
    val headers: MutableMap<String, String>,
    val body: String

) {

    override fun toString() = "$protocol $statusCode OK\r\n" + // Status Line
            headers.joinToString(": ", "\r\n") + // Headers
            "\r\n" + // Header End
            body

    private fun MutableMap<String, String>.joinToString(separator: String, entrySeparator: String): String {
        var str = ""
        for (entry in this.entries) {
            str += entry.key + separator + entry.value + entrySeparator
        }
        return str
    }
}
