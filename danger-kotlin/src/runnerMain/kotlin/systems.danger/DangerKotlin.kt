package systems.danger

import systems.danger.cmd.dangerfile.DangerFile
import systems.danger.utils.*

object DangerKotlin {
    private const val FILE_TMP_OUTPUT_JSON = "danger_out.json"

    fun run() {
        val dangerDSLPath = readLine()

        dangerDSLPath?.removePrefix("danger://dsl/")?.stripEndLine()?.let {
            val outputPath = FileSystem.createTempDirectory("danger_out").let {
                File(it.path, FILE_TMP_OUTPUT_JSON).path
            }

            with(DangerFile) {
                execute(it, outputPath.path)
            }

            printResult(outputPath)
        }
    }

    private fun printResult(outputPath: Path) {
        println("danger-results:/${outputPath.path}")
    }

    private fun String.stripEndLine() = trim('\u007F','\u0001', ' ')
}