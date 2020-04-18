package systems.danger.utils

internal fun log(level: LogLevel, message: String) {
    val logLevel = EnvConfigs.debug
    if (logLevel != LogLevel.Off && logLevel <= level) {
        println("[${level.name}] $message")
    }
}

internal fun debug(message: String) =
    log(LogLevel.Debug, message)
internal fun warn(message: String) =
    log(LogLevel.Warn, message)
internal fun info(message: String) =
    log(LogLevel.Info, message)
internal fun error(message: String) =
    log(LogLevel.Error, message)
internal fun verbose(message: String) =
    log(LogLevel.Verbose, message)

enum class LogLevel {
    Off, Error, Warn, Info, Debug, Verbose,
}