package systems.danger.kotlin

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import systems.danger.kotlin.sdk.DangerContext
import systems.danger.kotlin.sdk.DangerPlugin
import systems.danger.kotlin.sdk.Violation
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private fun FilePath.readText() = File(this).readText()

fun fromISO8601UTC(dateStr: String): Date? {
    val tz = TimeZone.getTimeZone("UTC")
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.timeZone = tz

    val alternativeDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    alternativeDf.timeZone = tz

    try {
        return df.parse(dateStr)
    } catch (e: ParseException) {
        try {
            return alternativeDf.parse(dateStr)
        } catch (e2: ParseException) {
            e.printStackTrace()
            e2.printStackTrace()
        }
    }

    return null
}

class Rfc3339DateJsonAdapter : JsonAdapter<Date>() {
    @Synchronized
    override fun fromJson(reader: JsonReader): Date {
        val string = reader.nextString()
        return fromISO8601UTC(string)!!
    }

    override fun toJson(writer: JsonWriter, value: Date?) {
        //Implementation not needed right now
    }
}

object register {
    internal var dangerPlugins = mutableListOf<DangerPlugin>()

    infix fun plugin(plugin: DangerPlugin) {
        dangerPlugins.add(plugin)
    }

    fun plugins(vararg pluginArgs: DangerPlugin) {
        dangerPlugins.addAll(pluginArgs)
    }
}

inline fun register(block: register.() -> Unit) = register.run(block)

inline fun danger(args: Array<String>, crossinline block: suspend DangerDSL.() -> Unit) = runBlocking(Dispatchers.Main) {
    Danger(args).block()
}

inline fun DangerDSL.onGitHub(onGitHub: GitHub.() -> Unit) {
    if (this.onGitHub) {
        github.run(onGitHub)
    }
}

inline fun DangerDSL.onGitLab(onGitLab: GitLab.() -> Unit) {
    if (this.onGitLab) {
        gitlab.run(onGitLab)
    }
}

inline fun DangerDSL.onBitBucket(onBitBucket: BitBucketServer.() -> Unit) {
    if (this.onBitBucketServer) {
        bitBucketServer.run(onBitBucket)
    }
}

inline fun DangerDSL.onGit(onGit: Git.() -> Unit) {
    git.run(onGit)
}

internal fun DangerPlugin.withContext(dangerContext: DangerContext) {
    context = dangerContext
}

private data class Credentials(
    @Json(name ="gitlab")
    val gitLab: GitLabAPICredentials?
)

private class DangerRunner(jsonInputFilePath: FilePath, jsonOutputPath: FilePath, jsonCredentials: String?) : DangerContext {

    val jsonOutputFile: File = File(jsonOutputPath)

    val danger: DangerDSL

    val dangerResults: DangerResults = DangerResults()

    override val fails: List<Violation>
        get() {
            return dangerResults.fails.toList()
        }
    override val warnings: List<Violation>
        get() {
            return dangerResults.warnings.toList()
        }
    override val messages: List<Violation>
        get() {
            return dangerResults.messages.toList()
        }
    override val markdowns: List<Violation>
        get() {
            return dangerResults.markdowns.toList()
        }

    private val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .add(KotlinJsonAdapterFactory())
        .build()

    init {
        this.danger = moshi.adapter(DSL::class.java).fromJson(jsonInputFilePath.readText())!!.danger
        this.danger.onGitLab {
            this.credentials = jsonCredentials?.let {
                moshi.adapter(Credentials::class.java).fromJson(it)?.gitLab
            }
        }

        register.dangerPlugins.forEach {
            it.withContext(this)
        }

        saveDangerResults()
    }

    //Api Implementation
    /**
     * Adds an inline fail message to the Danger report
     */
    override fun fail(message: String) {
        fail(Violation(message))
    }

    /**
     * Adds an inline fail message to the Danger report
     */
    override fun fail(message: String, file: FilePath, line: Int) {
        fail(Violation(message, file, line))
    }

    /**
     * Adds an inline warning message to the Danger report
     */
    override fun warn(message: String) {
        warn(Violation(message))
    }

    /**
     * Adds an inline warning message to the Danger report
     */
    override fun warn(message: String, file: FilePath, line: Int) {
        warn(Violation(message, file, line))
    }

    /**
     * Adds an inline message to the Danger report
     */
    override fun message(message: String) {
        message(Violation(message))
    }

    /**
     * Adds an inline message to the Danger report
     */
    override fun message(message: String, file: FilePath, line: Int) {
        message(Violation(message, file, line))
    }

    /**
     * Adds an inline markdown message to the Danger report
     */
    override fun markdown(message: String) {
        markdown(Violation(message))
    }

    /**
     * Adds an inline markdown message to the Danger report
     */
    override fun markdown(message: String, file: FilePath, line: Int) {
        markdown(Violation(message, file, line))
    }

    /**
     * Adds an inline suggest markdown message to the Danger report
     */
    override fun suggest(code: String, file: FilePath, line: Int) {
        if (dangerRunner.danger.onGitHub) {
            val message = "```suggestion\n $code \n```"
            markdown(Violation(message, file, line))
        } else {
            val message = "```\n $code \n```"
            message(Violation(message))
        }
    }

    private fun warn(violation: Violation) {
        dangerResults.warnings += (violation)
        saveDangerResults()
    }

    private fun fail(violation: Violation) {
        dangerResults.fails += violation
        saveDangerResults()
    }

    private fun message(violation: Violation) {
        dangerResults.messages += violation
        saveDangerResults()
    }

    private fun markdown(violation: Violation) {
        dangerResults.markdowns += violation
        saveDangerResults()
    }

    private fun saveDangerResults() {
        val resultsJSON = moshi.adapter(DangerResults::class.java).toJson(dangerResults)
        jsonOutputFile.writeText(resultsJSON)
    }
}

private lateinit var dangerRunner: DangerRunner

fun Danger(args: Array<String>): DangerDSL {
    val argsCount = args.count()

    val credentials = args.find { it.startsWith("credentials=") }

    val jsonInputFilePath = args[argsCount - 2]
    val jsonOutputPath = args[argsCount - 1]

    dangerRunner = DangerRunner(jsonInputFilePath, jsonOutputPath, credentials)
    return dangerRunner.danger
}

fun fail(message: String) =
    dangerRunner.fail(message)

fun fail(message: String, file: FilePath, line: Int) =
    dangerRunner.fail(message, file, line)

fun warn(message: String) =
    dangerRunner.warn(message)

fun warn(message: String, file: FilePath, line: Int) =
    dangerRunner.warn(message, file, line)

fun message(message: String) =
    dangerRunner.message(message)

fun message(message: String, file: FilePath, line: Int) =
    dangerRunner.message(message, file, line)

fun markdown(message: String) =
    dangerRunner.markdown(message)

fun markdown(message: String, file: FilePath, line: Int) =
    dangerRunner.markdown(message, file, line)

fun suggest(code: String, file: FilePath, line: Int) =
    dangerRunner.suggest(code, file, line)
