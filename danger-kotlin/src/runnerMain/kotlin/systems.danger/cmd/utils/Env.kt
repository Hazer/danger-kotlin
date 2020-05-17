package systems.danger.cmd.utils

import kotlinx.cinterop.toKString
import platform.posix.getenv

object Env {
    operator fun get(key: String): String? = getenv(key)?.toKString()
}