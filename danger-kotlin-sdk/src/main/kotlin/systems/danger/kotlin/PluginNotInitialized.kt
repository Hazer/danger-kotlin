@file:Suppress("NOTHING_TO_INLINE")

package systems.danger.kotlin

import systems.danger.kotlin.sdk.DangerPlugin
import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayList
import kotlin.reflect.KClass

@PublishedApi
internal class PluginNotInitialized(
    val pluginClass: KClass<out DangerPlugin>,
    val pluginId: String,
    cause: Throwable?
) : DangerException(cause) {
    override fun toString(): String {
        return "at DangerScript.<init>(${scriptFile}:${scriptLine})" +
                " You forgot to register the ${pluginClass.simpleName} plugin before using:" +
                "\n    Plugin className: ${pluginClass.qualifiedName}" +
                "\n    Plugin ID: $pluginId" +
                "\nPlease fix it by calling `register plugin ${pluginClass.simpleName}` before `danger(args)`"
    }
}

private inline fun <T> Iterable<T>.takeWhileInclusive(predicate: (T) -> Boolean): List<T> {
    val list = ArrayList<T>()
    for (item in this) {
        list.add(item)
        if (!predicate(item))
            break
    }
    return list
}

abstract class DangerException(
    cause: Throwable?
) : RuntimeException(cause) {
    companion object {
        private const val STARTING_POINT = "at Dangerfile_df.<init>(Dangerfile.df.kts:"
        const val FAILURE_TITLE = "DangerScript exception:\n"

        fun getTrace(throwable: Throwable?): String {
            if (throwable == null) return "null"

            return throwable.traceAsString().lines().takeWhileInclusive {
                !it.trim().startsWith(STARTING_POINT)
            }.joinToString("\n")
        }
    }
    private val error = cause?.stackTrace?.firstOrNull {
        it.fileName.endsWith("df.kts")
    }

    val scriptFile: String? = error?.fileName
    val scriptLine: Int? = error?.lineNumber

    override fun toString(): String {
        return "DangerScript exception:\n${scriptFile}:${scriptLine}:" +
                "\n${message}:\n```\n${getTrace(cause)}\n```"
    }
}

private inline fun Throwable.traceAsString(): String {
    return StringWriter().let {
        printStackTrace(PrintWriter(it))
        it.toString()
    }
}