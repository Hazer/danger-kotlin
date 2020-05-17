package systems.danger.utils

actual fun Path.isAbsolute(): Boolean = path.startsWith("/")