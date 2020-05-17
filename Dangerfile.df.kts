// Dangerfile.df.kts
/*
 * Use external dependencies using the following annotations:
 */
@file:Repository("https://repo.maven.apache.org")
// @file:Repository("https://jcenter.bintray.com")

@file:DependsOn("org.apache.commons:commons-text:1.6")

// @file:DependsOn("com.gianluz:danger-kotlin-android-lint-plugin:0.0.3")
// @file:DependsOn("io.vithor.danger.plugins:android-lint-plugin:0.0.1")
// @file:DependsOn("io.vithor.danger.plugins:detekt-plugin:0.0.5")

import org.apache.commons.text.WordUtils
import com.gianluz.dangerkotlin.androidlint.AndroidLint
import com.gianluz.dangerkotlin.androidlint.androidLint
import io.vithor.danger.plugins.detekt.Detekt
import io.vithor.danger.plugins.detekt.detekt
import systems.danger.kotlin.*

// register plugin MyDangerPlugin

register plugin AndroidLint
register plugin Detekt

danger(args) {

//    fail("I did fail before")


    val allSourceFiles = git.modifiedFiles + git.createdFiles
    val changelogChanged = allSourceFiles.contains("CHANGELOG.md")
    val codeChanges = allSourceFiles.firstOrNull { it.contains("src") }

    androidLint {
        reportOnly = allSourceFiles
        reportDirDistinct("build/reports/lint/xml", "*.xml")
//        throw IllegalArgumentException("Aow")
    }

    detekt(silentSkip = true, reportOnly = allSourceFiles) {
//       fileWildcards("*.xml")
    }

    onGitHub {
        val isTrivial = pullRequest.title.contains("#trivial")

        // Changelog
        if (!isTrivial && !changelogChanged && codeChanges != null) {
            warn(WordUtils.capitalize("any changes to library code should be reflected in the Changelog.\n\nPlease consider adding a note there and adhere to the [Changelog Guidelines](https://github.com/Moya/contributors/blob/master/Changelog%20Guidelines.md)."))
        }

        // Big PR Check
        if ((pullRequest.additions ?: 0) - (pullRequest.deletions ?: 0) > 300) {
            warn("Big PR, try to keep changes smaller if you can")
        }

        // Work in progress check
        if (pullRequest.title.contains("WIP", false)) {
            warn("PR is classed as Work in Progress")
        }
    }

    onGit {
        //No Java files check
        createdFiles.filter {
            it.endsWith(".java")
        }.forEach {
            // Using apache commons-text dependency to be sure the dependency resolution always works
            warn(WordUtils.capitalize("please consider to create new files in Kotlin"), it, 1)
        }
    }
    git.createdFiles.filter {
        it.endsWith(".gradle")
    }.forEach {
        // Using apache commons-text dependency to be sure the dependency resolution always works
        warn(WordUtils.capitalize("please consider to create new files in Kotlin DSL (.gradle.kts)"), it, 1)
    }
}
