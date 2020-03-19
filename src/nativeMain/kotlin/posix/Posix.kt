import kotlinx.cinterop.*
import platform.posix.*

object Posix {

    val R = platform.posix.O_RDONLY
    val RW = platform.posix.O_RDWR
    val C = platform.posix.O_CREAT

    /** ---------------------------------------------------------------
     * POSIX wrapper for File I/O
     */
    fun exist(path: String) = memScoped {
        with(alloc<platform.posix.stat>()) { platform.posix.stat(path, ptr) == 0 }
    }

    fun open(path: String, mode: Int) = platform.posix.open(path, mode)
    fun readAll(path: String) = run { var str = ""; read(path) { str += it }; str }
    fun read(path: String, block: (Char) -> Unit) =
        open(path, R).apply {
            do {
                val byte = nativeHeap.alloc<ByteVar>()
                val ln = platform.posix.read(this, byte.ptr, 1u)
                val char = byte.value.toChar()
                if (char != '\u0000') block(char)
            } while (ln > 0)
        }

    fun write(fd: Int, s: String) = platform.posix.write(fd, s.cstr, s.length.convert())
    fun write(fd: Int, s: Char) = write(fd, s + "")

    fun rename(from: String, to: String) = platform.posix.rename(from, to)

    fun mkdir(base: String, path: String) {
        var path = path
        if (path[path.length - 1] != '/') path += '/'
        var url = base;
        var fname = ""
        path.forEach {
            if (it == '/') {
                url += "/$fname"; fname = ""
            } else fname += it
            if (it == '/') {
                if (!exist(url)) platform.posix.mkdir(url, 777u)
            }
        }
    }


    // "$DB_PATH/asdfds"
    fun rmdir(path: String) {
        val dopen = opendir(path)
        do {
            val file = readdir(dopen)?.pointed?.d_name?.toKString()
            if (file != "." && file != ".." && file != null) {
                "$path/$file".let {
                    if (opendir(it) != null)
                        rmdir(it)
                    else remove(it)
                }
            }
        } while (file != null)
        remove(path)
    }
    /* -------------------------------------------------------------------- */
}
