package systems.danger.utils

import kotlinx.cinterop.CPointer
import platform.posix.*

actual class File actual constructor(actual val path: Path) {
    actual constructor(path: String) : this(Path(path))
    actual constructor(vararg paths: String) : this(
        Path(paths.first()).resolve(*paths.drop(1).toTypedArray())
    )

    actual fun exists() = FileSystem.fileExists(path)

    actual fun writeText(text: String, mode: String) {
        fopen(path.path, mode).use {
            if (fputs(text, it) < 0) {
                perror("The following error occurred while writing to file: ${path.path}\n")
                exit(1)
            }
        }
    }

    actual fun readText(): String? {
        return fopen(path.path, "r").use {
            val result = StringBuilder()
            do {
                val line = it.readLine()
                if (line != null) {
                    result.appendln(line)
                }
            } while (line != null)

            result.toString()
        }
    }

    actual fun readLines(): List<String> {
        return fopen(path.path, "r")?.use { file ->
            file.lineSequence().toList()
        } ?: emptyList()
    }

    actual fun forEachLine(line: (String) -> Unit) {
        fopen(path.path, "r").use { file ->
            file.lineSequence().forEach(line)
        }
    }

    actual fun findLine(line: (String) -> Boolean): String? {
        return fopen(path.path, "r").use { file ->
            file.lineSequence().find(line)
        }
    }
}

fun <R> CPointer<FILE>?.use(block: (CPointer<FILE>) -> R): R {
    requireNotNull(this) {
        perror("The following error occurred opening file")
        exit(1)
    }

    return try {
        block(this)
    } finally {
        try {
            fclose(this)
        } catch (closeEx: Throwable) {
            // ignore
        }
    }
}

fun CPointer<FILE>.readLine(): String? {
    var ch = getc(this)
    var lineBuffer: Array<Char> = kotlin.arrayOf()

    while ((ch != '\n'.toInt()) && (ch != EOF)) {
        lineBuffer += ch.toChar()

        ch = getc(this)
    }

    return when (lineBuffer.isEmpty()) {
        true -> null
        false -> lineBuffer.joinToString("")
    }
}

fun CPointer<FILE>.lineSequence(): Sequence<String> = LinesSequence(this).constrainOnce()

private class LinesSequence(private val reader: CPointer<FILE>) : Sequence<String> {
    override fun iterator(): Iterator<String> {
        return object : Iterator<String> {
            private var nextValue: String? = null
            private var done = false

            override fun hasNext(): Boolean {
                if (nextValue == null && !done) {
                    nextValue = reader.readLine()
                    if (nextValue == null) done = true
                }
                return nextValue != null
            }

            override fun next(): String {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                val answer = nextValue
                nextValue = null
                return answer!!
            }
        }
    }
}
