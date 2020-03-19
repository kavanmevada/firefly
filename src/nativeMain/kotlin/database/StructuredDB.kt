import StructuredDB.DB_PATH
import platform.posix.*

object StructuredDB {

    val DB_PATH = "/run/media/kavan/67A4-0823/kotlin-native/firestore-security/build/bin/linux/debugExecutable/database"

    // {key1:{key3:{key4:{key5:value5}}}, key2:value2}
    fun execute(path: String): String {
        return "$DB_PATH/$path".toJSON()
    }

    fun String.toJSON(): String { // Path must be absolute
        var isFirst = true
        var json = ""
        if (isdir) {
            json += "{"
            readdir(this) {
                with("$this/$it") {
                    if (!isFirst) json += "," else isFirst = false

                    if (isdir) {
                        json += "$it:"
                        val parse = toJSON()
                        if (parse != "{}") json += "$parse" else json += "null"
                    } else if (it == "0") json += readZeroFile
                }
            }
            json += "}"
        }

        // If URL ask for 0 file
        else if (zeroFileExist) "{$readZeroFile}"
        // If URL ask for JSON key
        else if (substringBeforeLast('/').zeroFileExist) {
            json += substringBeforeLast('/').parseZeroFile[substringAfterLast('/')]
        }

        return json
    }

    // Create Structure from JSON MutableMap
    fun setDB(parent: String, map: MutableMap<String, *>) {
        map.forEach {
            val url = (if (parent != "/") parent else "")

            //remove if structure exist with that key
            if (it.value is HashMap<*, *>) {
                ZeroFile(url).remove(it.key)
                setDB(url + '/' + it.key, it.value as HashMap<String, *>)
            } else ZeroFile(parent).set(it.key, it.value as String)
        }
    }


    // Loop through path
    fun String.pathWalker(with_base: String = "", run: (String) -> Unit) {
        var file_name = "";
        var url_path = with_base
        for (i in 0 until length) get(i).let {
            if (it != '/') file_name += get(i)
            if (it == '/' || i == length - 1) {
                url_path += '/' + file_name
                run(url_path)
                file_name = ""
            }
        }
    }

    fun String.dirWalker(run: (path: String, name: String) -> Unit) {
        readdir(this) {
            with("$this/$it") {
                if (isdir) {
                    run(this, it); dirWalker(run)
                }
            }
        }
    }

}


/********************************************
 *  Zero File
 ********************************************/

data class ZeroFile(private val url_path: String) {
    // Common Functions
    fun set(key: String, value: String) {
        val zero_file_path = "$DB_PATH$url_path/0"

        if (opendir("$DB_PATH$url_path/$key") != null)
            rmdir("$DB_PATH$url_path/$key")

        Posix.mkdir(DB_PATH, url_path)

        if (Posix.exist(zero_file_path)) modify(zero_file_path, key, value)
        else create(zero_file_path, key, value)
    }

    fun create(path: String, key: String, value: String) =
        Posix.write(Posix.open(path, Posix.RW or Posix.C), "{$key:$value}")

    fun remove(key: String) = remove("$DB_PATH$url_path/0", key)

    private fun remove(path: String, nkey: String) {
        if (Posix.exist(path)) {

            // Create temp file for modification
            val fd = Posix.open(path + '_', Posix.RW or Posix.C)

            Posix.write(fd, '{')

            // Parse
            var key = "";
            var value = ""
            var isKey = true
            Posix.read(path) {

                if (it == ':') isKey = false

                if (it != '{' && it != '}' && it != ',' && it != ':') {
                    if (isKey) key += it else value += it
                }

                if (it == ',' || it == '}') {
                    if (key != nkey) {
                        Posix.write(fd, "$key:$value")
                        if (it == ',') Posix.write(fd, ',')
                    }
                    key = ""; value = ""; isKey = true
                }
            }

            Posix.write(fd, '}')
            Posix.rename(path + '_', path)
        }
    }

    private fun modify(path: String, nkey: String, nvalue: String?) {
        // Create temp file for modification
        val fd = Posix.open(path + '_', Posix.RW or Posix.C)

        Posix.write(fd, '{')

        // Parse
        var key = ""
        var value = ""
        var isKey = true
        var isChnaged = false
        Posix.read(path) {

            if (it == ':') isKey = false

            if (it != '{' && it != '}' && it != ',' && it != ':') {
                if (isKey) key += it else value += it
            }

            if (it == ',' || it == '}') {

                if (key == nkey) isChnaged = true

                if (nvalue != null)
                    Posix.write(fd, "$key:${if (isChnaged) nvalue else value}")

                if (it == ',') Posix.write(fd, ',')
                key = ""; value = ""; isKey = true
            }
        }

        if (!isChnaged) Posix.write(fd, ",$nkey:$nvalue")

        Posix.write(fd, '}')

        Posix.rename(path + '_', path)
    }
}