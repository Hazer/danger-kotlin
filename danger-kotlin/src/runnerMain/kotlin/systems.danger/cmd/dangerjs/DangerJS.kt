package systems.danger.cmd.dangerjs

import systems.danger.cmd.Cmd

object DangerJS: DangerJSBridge {

    override fun process(command: String, processName: String, args: List<String>) {
        with(Cmd()) {
            name("$(which danger) $command --process $processName --passURLForDSL")
            args(args.joinToString(" "))
            exec()
        }
    }

    override fun version() {
        print("Danger.js version: ")
        with(Cmd()) {
            name("$(which danger) --version")
            args()
            exec(false)
        }
    }

    override fun resetStatus() {
        with(Cmd()) {
            name("$(which danger) reset-status")
            args()
            exec()
        }
    }
}