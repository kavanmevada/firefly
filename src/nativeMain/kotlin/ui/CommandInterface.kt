enum class Option { CONFIG, UUID, FORCE }
enum class OptionType { STRING, PATH, BOOL }
data class OptionValue(
    val type: OptionType,
    val name: String,
    val short: String?,
    val desc: String,
    val must: Boolean,
    val needValue: Boolean = false
)

class CommandInterface(vararg val options: Pair<Option, OptionValue>) {
    fun init(arguments: Array<String>): MutableMap<Option, String> {
        val user_arguments = mutableMapOf<Option, String>()
        arguments.forEachIndexed { index, arg ->
            var isForeignArg = true

            options.forEach {
                if (it.second.needValue) {
                    val value = arg.split('=')
                    if (value[0] == "--" + it.second.name || it.second.short?.let { value[0] == "-$it" } == true) {
                        isForeignArg = false
                        user_arguments.put(it.first, value[1])
                    }
                } else if (!it.second.needValue &&
                    (arg.contains("--${it.second.name}") || arg.contains("-${it.second.short}"))
                ) {
                    isForeignArg = false
                    if (arg != "--${it.second.name}" && arg != "-${it.second.short}") exit(options, 1)
                    else user_arguments.put(it.first, "true")
                }
            }

            if (isForeignArg) exit(options, 1)
        }

        options.forEach {
            if (it.second.must && !user_arguments.containsKey(it.first)) exit(options, 1)
            if (!it.second.needValue && !user_arguments.containsKey(it.first)) user_arguments.put(it.first, "false")
        }

        return user_arguments
    }

    fun exit(options: Array<out Pair<Option, OptionValue>>, statusCode: Int) {
        println("Usage: firestore [OPTIONS]\n" + "Check HTTP or HTTPs url requests are allowed or not.\n")
        println("Mandatory arguments to long options are mandatory for short options too.")

        var count = 0
        options.forEach {
            "--${it.second.name}${if (it.second.needValue) "=${it.second.type}" else ""}".length.let { size ->
                if (count <= size) count = size
            }
        }

        options.forEach {
            "--${it.second.name}${if (it.second.needValue) "=${it.second.type}" else ""}".let { name ->
                val paddedName = name + " ".repeat(count - name.length)
                println("\t" +
                        (it.second.short?.let { "-${it}, " } ?: "\t") +
                        "$paddedName\t${if (it.second.must) "* " else "  "}${it.second.desc}")
            }

        }
        platform.posix.exit(statusCode)
    }

}