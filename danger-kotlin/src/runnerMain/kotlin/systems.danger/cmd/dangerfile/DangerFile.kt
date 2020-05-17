package systems.danger.cmd.dangerfile

import systems.danger.cmd.*
import kotlinx.cinterop.CPointer
import platform.posix.*
import systems.danger.cmd.utils.credentials.CredentialsFetcher

object DangerFile: DangerFileBridge {
    private const val DANGERFILE_EXTENSION = ".df.kts"
    private const val DANGERFILE = "Dangerfile$DANGERFILE_EXTENSION"

    override fun execute(inputJson: String, outputJson: String) {
        val dangerfile = dangerfileParameter(inputJson) ?: DANGERFILE

        if(!dangerfile.endsWith(DANGERFILE_EXTENSION)) {
            println("The dangerfile is not valid, it must have '$DANGERFILE_EXTENSION' as extension")
            exit(1)
        }

        val args = mutableListOf(
            "-script-templates",
            "systems.danger.kts.DangerFileScript",
            "-cp",
            "/usr/local/lib/danger/danger-kotlin.jar",
            "-script",
            dangerfile
        )

        CredentialsFetcher.getCredentials()?.let {
            args += "credentials=${it.toArg()}"
        }

        args += inputJson
        args += outputJson

        Cmd().name("kotlinc").args(*args.toTypedArray()).exec()
    }
}

private fun dangerfileParameter(inputJson: String): String? {
    var result: String? = null

    fopen(inputJson, "r")?.apply {
        do {
            val line = readLine(this)?.let {
                val trimmedLine = it.trim()
                if (trimmedLine.startsWith("\"dangerfile\":")) {
                    val dangerFile = trimmedLine
                        .removePrefix("\"dangerfile\": \"")
                        .removeSuffix("\"")
                        .removeSuffix("\",")
                    result = dangerFile
                }
            }
        } while (line != null && result == null)
    }.also {
        fclose(it)
    }

    return result
}

private fun readLine(file: CPointer<FILE>): String? {
    var ch = getc(file)
    var lineBuffer: Array<Char> = arrayOf()

    while ((ch != '\n'.toInt()) && (ch != EOF)) {
        lineBuffer += ch.toChar()

        ch = getc(file)
    }

    when(lineBuffer.isEmpty()) {
        true -> return null
        false -> return lineBuffer.joinToString("")
    }
}