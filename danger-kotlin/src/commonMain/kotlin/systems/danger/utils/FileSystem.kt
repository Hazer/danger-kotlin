package systems.danger.utils

interface FileSystemProtocol {
    val pathSeparator: String

    val tempDirectory: Path

    var currentDirectory: Path

    val roots: List<Path>

    fun fileExists(path: Path): Boolean

    fun createDirectory(path: Path): Path

    fun createTempDirectory(template: String, prefix: String? = null): Path
}

expect object FileSystem : FileSystemProtocol