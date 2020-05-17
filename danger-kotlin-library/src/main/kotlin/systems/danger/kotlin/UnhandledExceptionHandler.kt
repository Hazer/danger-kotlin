package systems.danger.kotlin

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import systems.danger.kotlin.FilePath
import systems.danger.kotlin.sdk.Violation
import java.io.File
import java.util.*

internal class UnhandledExceptionHandler(jsonOutputPath: FilePath) {
    private val jsonOutputFile: File = File(jsonOutputPath)
    private val dangerResults: DangerResults = DangerResults()

    val fails: List<Violation>
        get() {
            return dangerResults.fails.toList()
        }

    private val moshi = Moshi.Builder()
        .add(
            Date::class.java, Rfc3339DateJsonAdapter()
                .nullSafe())
        .add(KotlinJsonAdapterFactory())
        .build()

    init {
        saveDangerResults()
    }

    //Api Implementation
    /**
     * Adds an inline fail message to the Danger report
     */
    fun fail(message: String) {
        fail(Violation(message))
    }

    /**
     * Adds an inline fail message to the Danger report
     */
    fun fail(message: String, file: FilePath, line: Int) {
        fail(Violation(message, file, line))
    }

    private fun fail(violation: Violation) {
        dangerResults.fails += violation
        saveDangerResults()
    }

    private fun saveDangerResults() {
        val resultsJSON = moshi.adapter(DangerResults::class.java).toJson(dangerResults)
        jsonOutputFile.writeText(resultsJSON)
    }
}