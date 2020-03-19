import kotlinx.cinterop.*
import platform.posix.*

val CONFIG_PATH = "/run/media/kavan/67A4-0823/kotlin-native/firefly/lock"
val UUID = "1234567890"


fun main() {

    // FireSecurity Init...
    val irfile = PsiFile(CONFIG_PATH)
    val rules = Builder(irfile).init()


    HttpServer(8082) {

        try {
            val request = it.reacieveAll()

            val requestedPath = request.requestheader.url.path


            val requestType = if (request.requestheader.method == "POST") "write"
            else if (request.requestheader.method == "GET") "read" else throw Exception("Unknown request type: ${request.requestheader.method}")
            val isAllowed = rules.matchURL(requestedPath, requestType)


            // Default Response
            var body = (
                    "<html>\n" +
                            "\t\t<head>\n" +
                            "\t\t<meta charset=\"UTF-8\">\n" +
                            "\t\t<title>My Web Page</title>\n" +
                            "\t\t<script>console.log(window.data)</script>\n" +
                            "\t</head>\n" +
                            "\t<script>document.write(window.data);</script>" +
                            "</html>\n")

            var response = HttpResponse(
                "HTTP/1.1", 404,
                hashMapOf(
                    "Content-Length" to "0",
                    "Content-Type" to "text/html",
                    "Connection" to "Closed"
                ),
                body
            ).toString()

            if (requestedPath.contains("favicon")) {

            } else if (isAllowed) {
                when (requestType) {
                    "write" -> {
                        val jsonMap = Json(request.body).parse() as MutableMap<String, *>
                        StructuredDB.setDB(request.requestheader.url.path, jsonMap)

                        response = HttpResponse(
                            "HTTP/1.1", 200,
                            hashMapOf(
                                "Content-Length" to "${body.length}",
                                "Content-Type" to "text/html",
                                "Connection" to "Closed"
                            ),
                            "<script>console.log(\"done!\")</script>\n$body"
                        ).toString()
                    }
                    "read" -> {
                        val json = StructuredDB.execute(request.requestheader.url.path)
                        body = "<script>window.data = \"$json\";</script>\n$body"

                        response = HttpResponse(
                            "HTTP/1.1", 200,
                            hashMapOf(
                                "Content-Length" to "${body.length}",
                                "Content-Type" to "text/html",
                                "Connection" to "Closed"
                            ),
                            body
                        ).toString()
                    }
                }
            } else if (!isAllowed) {
                response = HttpResponse(
                    "HTTP/1.1", 401,
                    hashMapOf(
                        "Content-Length" to "0",
                        "Content-Type" to "text/html",
                        "Connection" to "Closed"
                    ),
                    body
                ).toString()
            }

            write(it.socketFD, response.cstr, response.length.convert())
            close(it.socketFD)
        } catch (e: Exception) {
            println(e)
        }
    }.start(true)
}




fun MutableList<Rule>.matchURL(url: String, permissionType: String /* read, write*/): Boolean {
    val checkURL = url.drop(1).split('/')
    val variables = mutableMapOf<String, String>()

    var isAllowed = true

    // Grab matched URL list.
    forEach { rule ->
        val ruleURL = rule.url


        if (checkURL.size >= ruleURL.size) {
            // Check every part of URL
            var i = 0;
            var containsRecursive = false
            var matched = isAllowed
            val size = ruleURL.size
            do {
                val matchedURLPart = ruleURL[i]
                val checkURLPart = checkURL[i]

                if (matchedURLPart.isVal && matchedURLPart.value != checkURLPart)
                    matched = false
                else if (matchedURLPart.isVar) {
                    variables.put(matchedURLPart.value, checkURLPart)
                } else if (matchedURLPart.isRecursive) {
                    containsRecursive = true
                }

                i++
            } while (i < size && matched && !matchedURLPart.isRecursive)


            if (!containsRecursive && ruleURL.size != checkURL.size)
                matched = false



            if (matched && rule.type == permissionType) {
                println(rule)

                val lhs = rule.condition.value.lhs
                val rhs = rule.condition.value.rhs

                // Check for variable stored by that name
                // else take it as String
                val nLhs = variables.get(lhs?.value) ?: lhs?.value
                val nRhs = variables.get(rhs?.value) ?: rhs?.value

                isAllowed = rule.condition.value.cond?.invoke(nLhs, nRhs) ?: throw Exception("Error parsing condition.")
            }
        }
    } //------------------------

    return isAllowed
}
