package systems.danger.cmd.dangerfile

import systems.danger.cmd.*
import platform.posix.*
import systems.danger.utils.EnvConfigs
import systems.danger.utils.File

object DangerFile : DangerFileBridge {
    private const val DANGERFILE_EXTENSION = ".df.kts"
    internal const val DANGERFILE = "Dangerfile$DANGERFILE_EXTENSION"

    override fun execute(inputJson: String, outputJson: String) {
        val dangerfile = dangerfileParameter(inputJson) ?: DANGERFILE

        if (!dangerfile.endsWith(DANGERFILE_EXTENSION)) {
            println("The dangerfile is not valid, it must have '$DANGERFILE_EXTENSION' as extension")
            exit(1)
        }

        val args = mutableListOf(
            "-script-templates",
            "systems.danger.kts.DangerFileScript",
            "-cp",
            EnvConfigs.customProcessorJar,
            "-script",
            dangerfile
        )

        args += listOf(
            "other",
            "arguments",
            "cliArgs"
        )

        args += listOf(
            inputJson,
            outputJson
        )

        Cmd().name(EnvConfigs.customKotlincPath).args(*args.toTypedArray()).exec()
    }

    internal fun createDefaultDangerFile() {
        val dangerFile = File(DANGERFILE)
        if (dangerFile.exists()) {
            println("The $DANGERFILE already exists in this folder, just open it in your favorite editor")
            exit(1)
        }

        dangerFile.writeText(DEFAULT_DANGERFILE_TEMPLATE.trimIndent())
    }
}

private fun dangerfileParameter(inputJson: String): String? {
    val dangerFile = File(inputJson).findLine {
        val trimmedLine = it.trim()
        trimmedLine.startsWith("\"dangerfile\":")
    } ?: return null

    return dangerFile.removePrefix("\"dangerfile\": \"")
        .removeSuffix("\"")
        .removeSuffix("\",")
}
