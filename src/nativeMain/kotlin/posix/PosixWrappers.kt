import kotlinx.cinterop.*
import platform.posix.*

fun String.forEachChar(flag: Int, block: (Char) -> Unit) = nativeHeap.alloc<ByteVar>().let { byte ->
    val fd = open(this@forEachChar, flag)
    while (read(fd, byte.ptr, 1u) > 0) {
        block(byte.value.toChar())
    }
}

// POSIX
fun readdir(path: String, run: (String) -> Unit) = run {
    val dir = opendir(path)
    do {
        val fileName = readdir(dir)?.pointed?.d_name?.toKStringFromUtf8()
        fileName?.let {
            when (it) {
                ".", "..", null -> {
                }; else -> run(fileName)
            }
        }
    } while (fileName != null)
}

val String.isdir get() = opendir(this).let { closedir(it); it != NULL }


val String.zeroFileExist get() = open(this, O_RDONLY) != -1
val String.parseZeroFile
    get() = run {
        var key = "";
        var value = "";
        var isKey = true
        val map = mutableMapOf<String, String>()
        "$this/0".forEachChar(O_RDONLY) { char ->
            if (char == ':') isKey = false
            else if (char != '{' && char != '}' && char != ',' && char != ':' && char != '\n' && char != '\r') {
                if (isKey) key += char else value += char
            } else if (char == ',' || char == '}') {
                map[key] = value
                key = ""; value = ""; isKey = true
            }
        }
        map
    }

val String.readZeroFile
    get() = run {
        memScoped {
            val fd = open(this@run, O_RDONLY)
            val size = alloc<stat>().apply { stat(this@run, ptr) }.st_size
            val buff = ByteArray(size.toInt())
            lseek(fd, 1, SEEK_SET)
            read(fd, buff.refTo(0), (size - 2).toULong())
            buff.toKString()
        }
    }
