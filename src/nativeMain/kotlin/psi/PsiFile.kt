import kotlinx.cinterop.*
import platform.posix.*

// PsiFile
// ------------------------------------------------------
data class PsiFile internal constructor(val path: String, var name: String = path.substringAfterLast('/'))

val PsiFile.open get() = open(path, O_RDONLY)
val PsiFile.size get() = nativeHeap.alloc<stat>().apply { stat(path, ptr) }.st_size

// Extension -----------------
fun PsiFile.readUtf8() = run {
    val fd = open;
    val size = size
    mmap(NULL, size.convert(), PROT_READ, MAP_PRIVATE, fd, 0)?.readBytes(size.convert())
        ?.toKString() ?: throw Exception("Error reading lock file.")
} // ----------------------------------------------------

fun PsiFile.rawLoop(exe: (Byte) -> Unit) {
    val fd = open;
    val size = size
    val pointer = mmap(NULL, size.convert(), PROT_READ, MAP_PRIVATE, fd, 0)
    pointer?.reinterpret<ByteVar>()?.pointed?.ptr?.let { src ->
        var index = 0
        while (index < size) {
            exe(src[index])
            ++index
        }
    }
}
//-----------------------------------------------------------


open class PsiBuilder {

    private var size: Int = 0
    private var strData: String? = null
    private var pointer: CPointer<ByteVarOf<Byte>>? = null


    var marker = 0
    var jump = 0
    var pc: Char? = null
    var array = arrayOf(':', ';', ',', '.', '!', '=', '*', '{', '}', '(', ')', '/')

    var globalMarker = 0


    constructor(string: String) {
        size = string.length
        strData = string
    }

    constructor (psiFile: PsiFile) {
        val fd = psiFile.open
        size = psiFile.size.toInt()
        pointer = mmap(NULL, size.convert(), PROT_READ, MAP_PRIVATE, fd, 0)
            ?.reinterpret<ByteVar>()?.pointed?.ptr
    }

    val isEnd get() = marker >= size

    fun nextChar(): Char? {
        val c = pointer?.get(marker)?.toChar() ?: strData?.get(marker)

        val cc = when {
            pc == null && c == ' ' -> null.also { marker++; jump++ }
            pc == ' ' && c == ' ' -> null.also { marker++; jump++ }
            c == '\n' || c == '\r' -> null.also { marker++; jump++ }

            pc != ' ' && c in array -> ' '
            pc in array && c != ' ' -> ' '

            else -> c.also { marker++; jump++ }
        }?.also { pc = it }

        globalMarker++

        return cc
    }


    fun nextWord(updateMarker: Boolean): String? {
        var str = ""; jump = 0

        while (marker < size) {
            val c = nextChar()
            if (c == ' ') break
            else if (c != null) {
                str += c
            }
        }

        // rollback position
        if (!updateMarker) {
            marker -= jump; globalMarker -= jump
        }

        return if (str.length > 0) str else null
    }

    fun nextToken(updateMarker: Boolean) = nextWord(updateMarker)?.let { KToken(it) }

    val nameSpace
        get() = nextToken(true).let {
            if (it?.isKeyword == false) it.value
            else throw Exception("Found $it.value instead of non-keyword")
        }

    val nextToken get() = nextToken(true)

    fun _is(token: KToken, updateMarker: Boolean = true) = nextToken(updateMarker) == token

    fun _expect(token: KToken, msg: String) = nextToken(true).let {
        if (it != token) throw Exception("$msg but found ${it?.value} at $globalMarker") else Unit
    }
}













