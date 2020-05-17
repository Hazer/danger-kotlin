package systems.danger.utils

expect object Environment {
    operator fun get(key: String): String?
    fun getAll(): Map<String, String>
}