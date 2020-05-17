package systems.danger.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import org.gitlab4j.api.GitLabApiException

data class GitLabUtils internal constructor(private val gitLab: GitLab) {
    private val api = gitLab.gitLabApi?.repositoryFileApi

    suspend fun fileContents(path: String, repoSlug: String?, ref: String?): String {
        requireNotNull(api) {
            "GitLab credentials not provided"
        }

        val projectId = repoSlug ?: gitLab.metadata.repoSlug
        // Use the current state of PR if no ref is passed
        val desiredRef = ref ?: gitLab.mergeRequest.diffRefs.headSha

        try {
            return coroutineScope {
                val response =
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        api.getFile(
                            projectId,
                            path,
                            desiredRef
                        )
                    }

                response.decodedContentAsString
            }
        } catch (e: GitLabApiException) {
//            log("getFileContents", e)
            // GitHubAPI.fileContents returns "" when the file does not exist, keep it consistent across providers
            if (e.httpStatus == 404) {
                return ""
            }
            throw e
        }
    }
}