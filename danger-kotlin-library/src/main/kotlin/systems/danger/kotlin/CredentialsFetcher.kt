package systems.danger.kotlin

internal object CredentialsFetcher {
    private val env = Environment
    private const val DEFAULT_GITLAB_HOST = "https://gitlab.com"

    fun getCredentials(): Credentials =
        Credentials(
            gitlab = gitLabCredentials(),
            github = null,
            bitbucket = null
        )

    fun gitLabCredentials(): GitLabAPICredentials {
        val envHost = env["DANGER_GITLAB_HOST"]
        val envCIAPI = env["CI_API_V4_URL"]

        val host = when {
            !envHost.isNullOrBlank() -> {
                // We used to support DANGER_GITLAB_HOST being just the host e.g. "gitlab.com"
                // however it is possible to have a custom host without SSL, ensure we only add the protocol if one is not provided
                val protocolRegex = "/^https?:\\/\\//i".toRegex()
                if (protocolRegex.matches(envHost)) envHost else "https://${envHost}"
            }
            !envCIAPI.isNullOrBlank() -> {
                // GitLab >= v11.7 supplies the API Endpoint in an environment variable and we can work out our host value from that.
                // See https://docs.gitlab.com/ce/ci/variables/predefined_variables.html
                val hostRegex = "/^(https?):\\/\\/([^\\/]+)\\//i".toRegex()
                if (hostRegex.matches(envCIAPI)) {
                    val matches = hostRegex.find(envCIAPI)!!.groups
                    val matchProto = matches[1]
                    val matchHost = matches[2]
                    "${matchProto}://${matchHost}"
                } else {
                    DEFAULT_GITLAB_HOST
                }
            }
            else -> {
                DEFAULT_GITLAB_HOST
            }
        }

        return GitLabAPICredentials(
            host = host,
            token = env["DANGER_GITLAB_API_TOKEN"] ?: ""
        )
    }
}

internal data class GitLabAPICredentials(
    val host: String,
    val token: String
)

internal data class Credentials(
    val gitlab: GitLabAPICredentials?,
    val github: GitLabAPICredentials?,
    val bitbucket: GitLabAPICredentials?
)
