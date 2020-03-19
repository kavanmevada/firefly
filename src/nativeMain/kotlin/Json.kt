/**
 * ------------
 *  JSON Parser
 *  -----------
 *
 *  Fully Parse:                            JSON(Jsontxt).parse()
 *  Get Single Object:                      JSON(Jsontxt).getObject("ggg")
 *  From Hashmap or List to JSON String:    JsonObj.toJSONString()
 *
 *
 *  License:
 *  -----------------------------------------------------------
 *
 *  DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  Version 1, December 2019
 *
 *  Copyright (C) 2019 Kavan Mevada <kavanmevada@gmail.com>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *  DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 *  ----------------------------------------------------------
 */

private val sample = "{\n" +
        "    \"glossary\": {\n" +
        "        \"title\": \"example glossary\",\n" +
        "\t\t\"GlossDiv\": {\n" +
        "            \"title\": \"S\",\n" +
        "\t\t\t\"GlossList\": {\n" +
        "                \"GlossEntry\": {\n" +
        "                    \"ID\": \"SGML\",\n" +
        "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
        "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
        "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
        "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
        "\t\t\t\t\t\"GlossDef\": {\n" +
        "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
        "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
        "                    },\n" +
        "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"

class Json(private val str: String) {

    private var current = 0

    fun parse() = redirect()

    private fun parseObj(map: MutableMap<String, Any> = hashMapOf()): MutableMap<String, Any> {
        if (get() != '{' && get() != ',') throw Exception("missing { in string ${get()}"); next()

        val key = extract()

        if (get() != ':') throw Exception(": missing ${get()}"); next()

        map.put(key, redirect())

        return if (get() == ',') parseObj(map) else map
    }

    private fun parseArray(array: MutableList<Any> = mutableListOf()): MutableList<Any> {
        if (get() != '[' && get() != ',') throw Exception("missing [ in string ${get()}"); next()

        array.add(redirect())

        return if (get() == ',') parseArray(array) else array
    }

    private fun redirect() = when (get()) {
        '{' -> (parseObj(hashMapOf()) as Any).apply { next() }
        '[' -> (parseArray(mutableListOf()) as Any).apply { next() }
        else -> extract() as Any
    }

    private fun extract(): String {
        var value = ""

        val isString = (get() == '"')

        if (isString) current++

        loop@ do {
            val char = get()

            value += when {
                isString -> if (char == '"') break@loop else char
                else -> if (char.isSpecialChar()) break@loop else char
            }

            current++
        } while (current < str.length)

        if (isString) current++

        return value
    }

    fun getObject(key: String): Any {
        val index = str.indexOf(key)
        current = index + key.length + 1
        if (get() == '"') current++; next()
        return redirect()
    }

    private fun next() {
        do if (current + 1 >= str.length) break else current++
        while ((get() == '\n' || get() == ' ' ||
                    get() == '\t' || get() == '\r')
        )
    }

    // Helper functions
    private fun get() = str[current]
    private fun Char.isSpecialChar() =
        (this == '{' || this == '}' || this == '[' || this == '[' ||
                this == ']' || this == ',' || this == ':')
}

// Reverse
fun List<*>.toJSONString(): String {
    var str = "["
    var i = 0
    forEach { ele ->
        if (ele is MutableMap<*, *>) str += ele.toJSONString()

        if (i < size - 1) str += ","
        i++
    }
    str += "]"
    return str
}

fun MutableMap<*, *>.toJSONString(): String {
    var str = "{"
    var i = 0
    forEach {
        val key = it.key
        val value = it.value
        str += "\"${key}\":${when (value) {
            is List<*> -> value.toJSONString()
            is MutableMap<*, *> -> value.toJSONString()
            is String -> "\"$value\""
            else -> value
        }}"

        if (i < size - 1) str += ","
        i++
    }
    str += "}"
    return str
}
