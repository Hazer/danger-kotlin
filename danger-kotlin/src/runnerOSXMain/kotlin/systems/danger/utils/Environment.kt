package systems.danger.utils

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.getenv

actual object Environment {
    private val allEnvs: Map<String, String> by lazy {
        autoreleasepool {
            NSProcessInfo.processInfo.environment.map { it.key.toString() to it.value.toString() }.toMap()
        }
    }

//    private val allEnvsUpper: Map<String, String> by lazy { allEnvs.map { it.key.toUpperCase() to it.value }.toMap() }
//    actual operator fun get(key: String): String? = allEnvsUpper[key.toUpperCase()] ?: getenv(key)?.toKString()

    actual operator fun get(key: String): String? = allEnvs[key] ?: getenv(key)?.toKString()
    actual fun getAll() = allEnvs
}