package systems.danger.kotlin

import com.squareup.moshi.Json

data class Settings(
    @Json(name = "github")
    val gitHub: GitHubSettings?,
    @Json(name = "gitlab")
    val gitLab: GitLabSettings?,
    val cliArgs: CliArgs?
)

data class CliArgs(
    val base: String? = null,
    val textOnly: Boolean = false,
    val dangerfile: String? = null,
    val id: String? = null
)

data class GitHubSettings(
    @Json(name = "accessToken")
    val accessToken: String,
    val baseURL: String? = null
)

data class GitLabSettings(
    @Json(name = "accessToken")
    val accessToken: String,
    val baseURL: String?
)