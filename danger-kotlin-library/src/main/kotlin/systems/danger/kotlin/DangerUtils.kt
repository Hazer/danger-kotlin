package systems.danger.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class DangerUtils {
    private val fileMap = mutableMapOf<FilePath, String>()

    /**
     * Let's you go from a file path to the contents of the file
     * with less hassle.
     *
     * It specifically assumes golden path code so Dangerfiles
     * don't have to include error handlings - an error will
     * exit evaluation entirely as it should only happen at dev-time.
     *
     * @param path the file reference from git.modified/created/deleted etc
     * @return the file contents, or bails
     */
    suspend fun readFile(path: FilePath): String {
        fileMap[path]?.let { contents -> return contents }

        // Otherwise grab it from the FS
        return withContext(Dispatchers.IO) {
            val file = File(path).takeIf { it.exists() } ?: throw FileNotFoundException("File with $path not found.")

            file.readText().also { contents ->
                fileMap[path] = contents
            }
        }
    }

    suspend fun fileExists(path: FilePath): Boolean {
        fileMap[path]?.let { contents -> return contents.isNotBlank() }

        // Otherwise grab it from the FS
        return withContext(Dispatchers.IO) {
            File(path).exists()
        }
    }

    /**
     * Returns the line number of the lines that contain a specific string in a file
     *
     * @param string The string you want to search
     * @param file The file path of the file where you want to search the string
     * @return the line number of the lines where the passed string is contained
     */
    suspend fun linesWith(string: String, file: FilePath): List<Int> {
        val result = mutableListOf<Int>()

        return withContext(Dispatchers.IO) {
            val lines = readFile(file).lines()

            lines.forEachIndexed { index, line ->
                if (line.contains(string)) {
                    result.add(index + 1)
                }
            }

            result
        }
    }

    /**
     * Returns the line number of the lines that contain a specific string in a file
     *
     * @param regex The regex you want to search
     * @param file The file path of the file where you want to search the string
     * @return the line number of the lines where the passed string is contained
     */
    suspend fun linesWith(regex: Regex, file: FilePath): List<Int> {
        val result = mutableListOf<Int>()

        return withContext(Dispatchers.IO) {
            val lines = readFile(file).lines()

            lines.forEachIndexed { index, line ->
                if (line.matches(regex)) {
                    result.add(index + 1)
                }
            }

            result
        }
    }

    /**
     * Gives you the ability to cheaply run a command and read the
     * output without having to mess around
     *
     * It generally assumes that the command will pass, as you only get
     * a string of the STDOUT. If you think your command could/should fail
     * then you want to use `spawn` instead.
     *
     * @param command The first part of the command
     * @param args An optional array of arguments to pass in extra
     * @return the stdout from the command
     */
    suspend fun exec(command: String, args: Array<String> = emptyArray()): String {
        return ShellExecutor().exec(command, args)
    }

    /**
     * Gives you the ability to cheaply run a command and read the
     * output without having to mess around too much, and exposes
     * command errors in a pretty elegant way.
     *
     * @param command The first part of the command
     * @param args An optional array of arguments to pass in extra
     * @return the stdout from the command
     */
    suspend fun spawn(command: String, args: Array<String> = emptyArray()): String {
        return ShellExecutor().spawn(command, args)
    }
}
