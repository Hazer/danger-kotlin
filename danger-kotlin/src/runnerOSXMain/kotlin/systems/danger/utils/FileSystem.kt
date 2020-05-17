package systems.danger.utils

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory
import platform.posix.*

actual typealias FileSystem = MacFileSystem

object MacFileSystem : FileSystemProtocol {

    override val pathSeparator: String = "/"

    override val tempDirectory: Path
        get() = Path(
            NSFileManager.defaultManager.temporaryDirectory.path
                ?: tmpnam(null)?.toKString()
                ?: "/tmp/"
        )

    override var currentDirectory: Path
        get() = Path(NSFileManager.defaultManager.currentDirectoryPath)
        set(value) {
            NSFileManager.defaultManager.changeCurrentDirectoryPath(value.path)
        }

    override val roots: List<Path> = listOf(Path("/"))

    override fun fileExists(path: Path): Boolean {
        return access(path.path, F_OK) != -1
    }

    override fun createDirectory(path: Path): Path {
        val res = mkdir(path.path, "775".toUInt(8).convert())
        if (res == -1) {
            val errno = errno

            throw IllegalStateException(
                "Failed to create dir ${path.path} with error code $errno, ${
                strerror(errno)?.toKString() ?: "Unknown $errno error"
                }"
            )
        }
        return path
    }

    override fun createTempDirectory(template: String, prefix: String?): Path {
        var start = prefix ?: tempDirectory.path

        if (!start.startsWith("/")) {
            start = "/$start"
        }

        if (!start.endsWith("/")) {
            start += "/"
        }

        val path = "$start${template}_XXXXXXXXXXXXXXXXXXXX"
        memScoped {
            val dirPath = mktemp(path.cstr)!!
            val dirString = dirPath.toKString()

            return Path(dirString).also {
                createDirectory(it)
            }
        }
    }
}