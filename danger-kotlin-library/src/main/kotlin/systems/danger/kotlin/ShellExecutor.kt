package systems.danger.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

internal class ShellExecutor {

    suspend fun exec(command: String, args: Array<String>): String = withContext(Dispatchers.IO) {
        val process = ProcessBuilder().apply {
            command(command, *args)
            redirectErrorStream(false)
        }.start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))

        process.waitFor()

        return@withContext reader.readText()
    }

    suspend fun spawn(command: String, args: Array<String>): String = withContext(Dispatchers.IO) {
        val process = ProcessBuilder().apply {
            command(command, *args)
        }.start()

        val exitCode = process.waitFor()

        val stdOut = BufferedReader(InputStreamReader(process.inputStream)).readText()
        return@withContext if (exitCode == 0) {
            stdOut
        } else {
            val error = BufferedReader(InputStreamReader(process.inputStream))

            throw SpawnException(
                command,
                exitCode,
                stdout=stdOut,
                stderr=error.readText()
            )
        }
    }
}

data class SpawnException(val command: String, val exitCode: Int, val stdout: String, val stderr: String) : Throwable()
