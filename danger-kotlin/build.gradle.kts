plugins {
    kotlin("multiplatform")
}

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    var isOSX = false
    val hostTarget = when (val hostOs = System.getProperty("os.name")) {
        "Mac OS X" -> {
            isOSX = true
            macosX64("runner")
        }
        "Linux" -> linuxX64("runner")
        else -> throw GradleException("Host OS '$hostOs' is not supported.") as Throwable
    }

    val linux = linuxX64("runnerLinux")

    val targetBase = if (isOSX) {
        macosX64("runnerOSX")
    } else linux

    hostTarget.apply {
        binaries {
            executable()
        }
    }

    println(targets.names)

    sourceSets {
        val commonMain by getting {
            println(kotlin.srcDirs)
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val runnerMain by getting {
            dependsOn(commonMain)
            val sourceName = "${targetBase.name}Main"
            dependsOn(getByName(sourceName))
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val runnerLinuxMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val runnerTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}