val Interface = CommandInterface(
    Option.UUID to OptionValue(OptionType.STRING, "uuid", null, "Logged-In user's unique ID", true, true),
    Option.CONFIG to OptionValue(OptionType.PATH, "config", "c", "Security Config File Path", true, true),
    Option.FORCE to OptionValue(OptionType.BOOL, "force", "f", "Force apply", false, false)
)

fun main2(args: Array<String>) = FireSecurityMain(Interface.init(args))
data class Auth(val uid: String)
data class Request(val auth: Auth, val resources: MutableMap<String, String>?)


fun FireSecurityMain(arguments: MutableMap<Option, String>) {

    val config_path = arguments.get(Option.CONFIG) ?: throw Exception("Configuration file not found!")
    val auth = Auth(arguments.get(Option.UUID) ?: throw Exception("UUID not found!"))
    val request = Request(auth, null)


    val irfile = PsiFile(config_path)
    val rules = Builder(irfile).init()
    //val matchedRule = rules.matchURL("/databases1/databaseName/documents/kavadfew/afaf/adfasd")

    //val result = matchedRule?.allowed("read") ?: throw Exception("NULL result, Unknown Error type.")

//    if (!result) {
//        matchedRule.rules[0].let {
//            var url = ""; it.url.forEach { url+="/${it.value}" }
//            println("Error on rule: $url -> ${it.type}")
//            //it.condition.value.let { println("${it.lhs?.value} ${it.sign?.value} ${it.rhs?.value}") }
//        }
//    } else println(true)


    //println(arguments)
}
