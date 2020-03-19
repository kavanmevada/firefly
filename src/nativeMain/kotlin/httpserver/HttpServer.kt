import kotlinx.cinterop.*
import platform.posix.*

// Universal...
val String.toLog get() = println(this)

class HttpServer(val port: Int, val backlog: Int = 50, val execBlock: (HttpCall) -> Unit) {

    private var socketFd = create()
    private val socketAddr = socketAddressV4()

    fun start(wait: Boolean) {
        do {
            val clientSock = accept()
            execBlock(HttpCall(clientSock))
        } while (wait)
    }

    init {
        socketAddr.watch(socketFd)
    }

    fun socketAddressV4(): sockaddr_in {
        return memScoped {
            alloc<sockaddr_in>().apply {
                sin_family = AF_INET.toUShort() // Protocol Family
                sin_port = htons(port.toUShort()) // Port number
                sin_addr.s_addr = INADDR_ANY // AutoFill local address
            }
        }
    }

    fun create() = socket(AF_INET, SOCK_STREAM, 0).also {
        it.ensureNotMinusOne("Failed to obtain Socket Descriptor.")
        "[Server] Socket created sucessfully.".toLog
    }

    fun Int.listen() = listen(this, backlog).also {
        it.ensureNotMinusOne("Failed to listen Port.")
        "[Server] Listening the port $port successfully.".toLog
    }

    fun sockaddr_in.watch(fd: Int): Int {
        return bind(fd, this.reinterpret<sockaddr>().ptr, sockaddr_in.size.toUInt()).also {
            it.ensureNotMinusOne("Failed to bind Port.")
            "[Server] Binded tcp port $port in addr 127.0.0.1 sucessfully.".toLog
            fd.listen() // Listen on port
        }
    }

    //     Wait a connection, and obtain a new socket file despriptor for single connection
//     @return Client's Socket FD
    fun accept() = accept(socketFd, null, null).also {
        it.ensureNotMinusOne("Obtaining new Socket Despcritor.")
        "[Server] Server has got connected from ${socketAddr.getConnectedAddress()}.".toLog
    }

    // TODO implement readable
    fun sockaddr_in.getConnectedAddress() = sin_addr.s_addr

    // Error Handling....
    fun Int.ensureNotMinusOne(error: String) {
        if (this == -1) throw IOException("$error (errno = $errno)")
    }

    // Exceptions....
    class IOException(message: String) : RuntimeException(message)
}
