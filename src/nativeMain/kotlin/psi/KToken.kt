data class KToken(val value: String) {
    init {
        if (value == "") throw Exception("Trying to read empty token!")
    }

    companion object {
        val MATCH = KToken("match")
        val ALLOW = KToken("allow")
        val FUN = KToken("fun")

        val IF = KToken("if")
        val DIV = KToken("/")
        val LBRACE = KToken("{")
        val RBRACE = KToken("}")

        val LBRACKET = KToken("(")
        val RBRACKET = KToken(")")


        val IS_EQ = KToken("==")
        val NOT_EQ = KToken("!=")

        val COMMA = KToken(",")
        val COLON = KToken(":")

        val DOT = KToken(".")

        val SEMICOLON = KToken(";")

        val EXLEMATION = KToken("!")
        val EQ = KToken("=")

        val STAR = KToken("*")

        val KEYWORDS = setOf(
            FUN, MATCH, ALLOW, IF, DIV, LBRACE, RBRACE, EQ, COMMA, COLON, SEMICOLON, DOT, EXLEMATION, STAR
        )

    }

    val isKeyword: Boolean
        get() {
            KEYWORDS.forEach { if (it == this) return true }
            return false
        }
}



