// http_URL = "http:" "//" host [ ":" port ] [ abs_path [ "?" query ]]
data class HttpURL(
    var protocol: String = "",
    var host: String = "",
    var port: String = "",
    var path: String = "",
    var queries: String = ""
) {

    companion object {
        fun parse(url: String): HttpURL {
            var slashCount = 0
            var colonCount = 0
            var qustionCount = 0

            val url2 = HttpURL()

            for (i in 0..url.length - 1) {

                val char = url.get(i)
                val decimal = char.decimal

                when (decimal) {
                    47 /* / */ -> slashCount++
                    58 /* : */ -> colonCount++
                    63 /* ? */ -> qustionCount++
                }

                when {
                    slashCount == 0 && colonCount == 0 && qustionCount == 0 -> if (decimal.isText) url2.protocol += char
                    slashCount == 2 && colonCount == 1 && qustionCount == 0 -> if (decimal.isAllowed(Type.HOST)) url2.host += char
                    slashCount == 2 && colonCount == 2 && qustionCount == 0 -> if (decimal.isAllowed(Type.PORT)) url2.port += char
                    slashCount > 2 && qustionCount == 0 -> if (decimal.isAllowed(Type.PATH)) url2.path += char
                    slashCount > 2 && qustionCount == 1 -> if (decimal.isAllowed(Type.QUERY)) url2.queries += char
                }
            }

            return url2
        }

        enum class Type { HOST, PORT, PATH, QUERY }

        private fun Int.isAllowed(type: Type): Boolean {
            return when (type) {
                Type.HOST -> (this.isText || this == 46)
                Type.PORT -> this.isNumber
                Type.PATH -> (this.isText || this.isNumber || this == 46 || this == 47 || this == 95)
                Type.QUERY -> (this.isText || this.isNumber || this == 61 || this == 38)
            }
        }

        private val Char.decimal get() = this - '0' + 48
        private val Int.isText get() = this in 97..122 || this in 65..90
        private val Int.isNumber get() = this in 48..57
    }

    override fun toString() = "$protocol://$host:$port$path" + if (queries != "") "?$queries" else ""
}

// fun Char.toLowerCase() = (this-'0') and 65503
// fun Char.small() = with((this-'0') and 65503) { if (this >= 10) 'A' + this - 10 else '0' + this }
// val Int.char get() = if (this >= 10) 'A' + this - 10 else '0' + this
