import systems.danger.DangerKotlin
import systems.danger.cmd.dangerfile.DangerFile
import systems.danger.cmd.dangerjs.DangerJS
import systems.danger.utils.debug

const val PROCESS_DANGER_KOTLIN = "danger-kotlin"
const val VERSION = "0.5.1"

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (val command = args.first()) {
            "init" -> DangerFile.createDefaultDangerFile()
            "ci", "local", "pr" -> {
                debug(
                    "Calling DangerJS.process() for $command" +
                            " with args: [${args.joinToString(", ")}]"
                )
                DangerJS.process(command, PROCESS_DANGER_KOTLIN, args.drop(1))
            }
            "runner" -> {
                debug(
                    "runner: Calling DangerKotlin.run() with args: [${args.joinToString(
                        ", "
                    )}]"
                )
                DangerKotlin.run()
            }
            "reset-status" -> DangerJS.resetStatus()
            "--version" -> {
                println(dangerVersion())
                DangerJS.version()
            }
            else -> return
        }
    } else {
        debug("Calling DangerKotlin.run() with empty args")
        DangerKotlin.run()
    }
}

private fun dangerVersion(): String {
    val lineEnding = VERSION.indices.joinToString("") { "=" }
    return """

=======================$lineEnding
danger-kotlin version: $VERSION
=======================$lineEnding
""".trimIndent()
}
