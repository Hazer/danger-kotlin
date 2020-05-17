package systems.danger.utils

import kotlinx.cinterop.*
import platform.posix.*

actual typealias FileSystem = LinuxFileSystem

object LinuxFileSystem : FileSystemProtocol {
    override val pathSeparator: String = "/"

    override val tempDirectory: Path by lazy {
        var path: String? = null

        for (v in listOf("TMPDIR", "TMP", "TEMP", "TEMPDIR")) {
            path = Environment[v]
            if (path != null) break
        }

        Path(path ?: "/tmp")
    }

    override var currentDirectory: Path
        get() {
            memScoped {
                val ptr = allocArray<ByteVar>(4096)
                return Path(getcwd(ptr, 4096.convert())?.toKString() ?: throw IOException.fromErrno("directory"))
            }
        }
        set(value) {
            chdir(value.path)
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