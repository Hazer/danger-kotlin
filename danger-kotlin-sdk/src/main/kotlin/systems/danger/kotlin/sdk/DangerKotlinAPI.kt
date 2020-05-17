@file:Suppress("NOTHING_TO_INLINE")

package systems.danger.kotlin.sdk

import systems.danger.kotlin.DangerException
import systems.danger.kotlin.PluginNotInitialized

interface DangerContext {
    fun message(message: String)
    fun message(message: String, file: String, line: Int)
    fun markdown(message: String)
    fun markdown(message: String, file: String, line: Int)
    fun warn(message: String)
    fun warn(message: String, file: String, line: Int)
    fun fail(message: String)
    fun fail(message: String, file: String, line: Int)
    fun suggest(code: String, file: String, line: Int)

    val fails: List<Violation>
    val warnings: List<Violation>
    val messages: List<Violation>
    val markdowns: List<Violation>
}

data class Violation(
    val message: String,
    val file: String? = null,
    val line: Int? = null
)

object Sdk {
    const val VERSION_NAME = "1.2"
    const val API_VERSION = 3
}

abstract class DangerPlugin {
    companion object {
        const val DEVELOPED_WITH_API = Sdk.API_VERSION
    }

    abstract val id: String
    lateinit var context: DangerContext

    protected fun handleException(exception: Throwable, extraInfo: Map<String, String> = emptyMap()) {
        if (!this::context.isInitialized) {
            throw PluginNotInitialized(this::class, id, exception)
        }

        val trace = DangerException.getTrace(exception)

        var message = "PLUGIN-ERROR: " +
                "$id failed, we are skipping its analysis, " +
                "fix the plugin or open an issue in the repository." +
                "\nMessage: ${exception.message}" +
                "\nSdk: ${Sdk.VERSION_NAME}"

        if (extraInfo.isNotEmpty()) {
            message += "\nInfo: "
            extraInfo.forEach { (key, value) ->
                message += "$key: $value"
            }
            message += "\n"
        }

        message += "\nStackTrace:\n```\n$trace\n```\n"

        context.fail(message)
    }
}