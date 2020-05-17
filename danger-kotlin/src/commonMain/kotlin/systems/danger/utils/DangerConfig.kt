package systems.danger.utils

internal const val DANGER_KOTLIN_LOCAL_JAR = "/usr/local/lib/danger/danger-kotlin.jar"

internal const val ENV_DANGER_KOTLIN_JAR = "DANGER_KOTLIN_JAR"
internal const val ENV_DANGER_KOTLINC = "DANGER_KOTLINC"
internal const val ENV_DEBUG = "DEBUG"

data class DangerConfig(
    val debug: LogLevel,
    val customKotlincPath: String,
    val customProcessorJar: String
) {
    operator fun get(key: String): String? {
        return when (key) {
            ENV_DANGER_KOTLIN_JAR -> Environment[ENV_DANGER_KOTLIN_JAR] ?: customProcessorJar
            ENV_DANGER_KOTLINC -> Environment[ENV_DANGER_KOTLINC] ?: customKotlincPath
            ENV_DEBUG -> Environment[ENV_DEBUG] ?: debug.toString()
            else -> null
        }
    }
}

val EnvConfigs by lazy { dangerConfigs() }

internal fun dangerConfigs(): DangerConfig {
    var debug = Environment[ENV_DEBUG].toLogLevel()
    var customCompilerPath = Environment[ENV_DANGER_KOTLINC] ?: "kotlinc"
    var customProcessor = Environment[ENV_DANGER_KOTLIN_JAR] ?: DANGER_KOTLIN_LOCAL_JAR

//    val dir = opendir("./")
//    if (dir != null) {
//        try {
//            var ep = readdir(dir)
//            while (ep != null) {
//                val name = ep.pointed.d_name.toKString()
////                println(ep.pointed.d_name.toKString())
//                if (name == ".dangerConfig") {
//                    return readFile(ep)
//                }
//                ep = readdir(dir)
//            }
//        } finally {
//            closedir(dir)
//        }
//    }

    val configFile = File("$cwd/.dangerConfig").takeIf { it.exists() }
        ?: File("~/.dangerConfig").takeIf { it.exists() }

    configFile?.forEachLine {
        val trimmedLine = it.trim()
        if (!trimmedLine.startsWith('#')) {
            val item = trimmedLine.split('=')
            when (item[0]) {
                "debug" -> debug = item[1].toLogLevel()
                "kotlinc_path" -> customCompilerPath = item[1]
                "danger_processor_jar" -> customProcessor = item[1]
            }
        }
    }

    return DangerConfig(
        debug = debug,
        customKotlincPath = customCompilerPath,
        customProcessorJar = customProcessor
    )
}

private fun String?.toLogLevel(): LogLevel {
    return when (this) {
        "*" -> LogLevel.Verbose
        "true", "debug" -> LogLevel.Debug
        "info" -> LogLevel.Info
        "warn" -> LogLevel.Warn
        "error" -> LogLevel.Error
        else -> LogLevel.Off
    }
}

val cwd get() = FileSystem.currentDirectory.path

//val cwd: String
//    get() {
//        val cwd = ByteArray(1024)
//        cwd.usePinned {
//            getcwd(it.addressOf(0), 1024.convert())
//        }
//        return cwd.toKString()
//    }