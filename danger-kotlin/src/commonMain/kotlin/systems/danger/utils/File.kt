package systems.danger.utils

expect class File {
    val path: Path
    fun exists(): Boolean

    constructor(path: Path)
    constructor(path: String)
    constructor(vararg paths: String)

    fun writeText(text: String, mode: String = "w")
    fun readText(): String?
    fun readLines(): List<String>
    fun forEachLine(line: (String) -> Unit)

    fun findLine(line: (String) -> Boolean): String?
}

class IOException(val errno: String) : Exception() {
    companion object {
        fun fromErrno(errno: String): IOException {
            return IOException(errno)
        }
    }
}