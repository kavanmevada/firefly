class Builder(psiFile: PsiFile) : PsiBuilder(psiFile) {
    val functions = mutableMapOf<String, LExpression<LType>>()
    fun init() = mutableListOf<Rule>().apply { parseBlock(this) }
}

fun Builder.parseBlock(rules: MutableList<Rule>, parentURL: MutableList<LType> = mutableListOf()) {

    val token = nextToken
    when (token) {
        KToken.MATCH -> {
            //Parse URL
            while (nextToken(false) == KToken.DIV) { //Parse URL
                nextToken // skip /
                val instance = parseType()
                parentURL.add(instance)
            }
            _expect(KToken.LBRACE, "{ is expected after URL")

            if (!isEnd) parseBlock(rules, parentURL)
        }

        KToken.ALLOW -> {
            // Parse Allow Block
            val list = mutableListOf<String>()

            list.add(nameSpace)
            if (_is(KToken.COMMA)) {
                list.add(nameSpace)
                _expect(KToken.COLON, ": is expected after read or write")
            }
            val conditionBlock = parseCondition()

            list.forEach { rules.add(Rule(parentURL.toList(), it, conditionBlock)) }

            _expect(KToken.SEMICOLON, "; is expected after end of allow")

        }

        KToken.FUN -> {
            val funName = nameSpace
            _expect(KToken.LBRACKET, "( is expected after function name")
            _expect(KToken.RBRACKET, ") is expected after function name")
            _expect(KToken.LBRACE, "{ is expected for function body")
            val conditionBlock = parseExpression()
            functions.put(funName, conditionBlock)
        }

    }

    if (_is(KToken.RBRACE, false)) {
        nextToken; parentURL.clear()
    }

    if (!isEnd) parseBlock(rules, parentURL)

}


fun Builder.parseCondition(): LExpression<LType> {
    _expect(KToken.IF, "Only IF is implemented")
    return parseExpression()
}

fun Builder.parseExpression(): LExpression<LType> {
    var cond: ((lhs: String?, rhs: String?) -> Boolean)? = null
    var rhs: LType? = null
    var sign: KToken? = null

    val lhs = parseType()

    when (nextToken) {
        KToken.EXLEMATION -> {
            _expect(KToken.EQ, "= is expected after \"! or =\"")
            rhs = parseType(); sign = KToken.NOT_EQ
            cond = { lhs, rhs -> lhs != rhs }
        }
        KToken.EQ -> {
            _expect(KToken.EQ, "= is expected after \"! or =\"")
            rhs = parseType(); sign = KToken.EQ
            cond = { lhs, rhs -> lhs == rhs }
        }
        KToken.LBRACKET -> {
            _expect(KToken.RBRACKET, ") is expected")
            functions.get(lhs.value)?.let { return LExpression(it.value) }
        }
    }

    return LExpression(LCondition(lhs, rhs, cond, sign))
}


fun Builder.parseType(): LType {
    val instance = nextToken
    var type = Type.VAL
    val value = when {
        instance == KToken.LBRACE -> {
            type = Type.VAR
            nameSpace.also {
                val ntoken = nextToken
                when {
                    ntoken == KToken.EQ -> {
                        type = Type.RECURSIVE_VAR
                        _expect(KToken.STAR, "** is expected")
                        _expect(KToken.STAR, "* is expected after *")
                        _expect(KToken.RBRACE, "'}' is expected after \"variable\"")
                    }
                    ntoken != KToken.RBRACE -> throw Exception("'}' is expected after \"variable\"")
                }
            }
        }

        instance == KToken("request") -> {
            _expect(KToken.DOT, ". is expected after \"request\"")
            val name = nameSpace

            if (name == "auth") {
                _expect(KToken.DOT, ". is expected after \"request\"")
                if (nameSpace == "uid") {
                    UUID
                } else throw Exception("auth.$nameSpace not implemented")
            } else if (name == "resource") {
                _expect(KToken.DOT, ". is expected after \"request\"")
                UUID ?: throw Exception("resource.$nameSpace not implemented")
            } else throw Exception("auth is only implemented")
        }

        instance != null -> instance.value

        else -> "null"
    }

    return LType(type, value)
}


data class Rule(val url: List<LType>, val type: String, val condition: LExpression<LType>)

enum class Type { VAL, VAR, RECURSIVE_VAR }
data class LType(private val type: Type, val value: String) {
    val isVal get() = type == Type.VAL
    val isVar get() = type == Type.VAR
    val isRecursive get() = type == Type.RECURSIVE_VAR
}
data class LExpression<T>(val value: LCondition<T>)
data class LCondition<T>(
    val lhs: T?,
    val rhs: T?,
    val cond: ((lhs: String?, rhs: String?) -> Boolean)?,
    val sign: KToken?
)