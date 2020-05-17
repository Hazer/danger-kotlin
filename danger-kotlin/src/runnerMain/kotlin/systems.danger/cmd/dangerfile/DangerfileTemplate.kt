package systems.danger.cmd.dangerfile

internal const val DEFAULT_DANGERFILE_TEMPLATE = """
            // ${DangerFile.DANGERFILE}
            /*
             * Use external dependencies using the following annotations:
             */
            // @file:Repository("https://repo.maven.apache.org")
            // @file:DependsOn("org.apache.commons:commons-text:1.6")

            // import org.apache.commons.text.WordUtils
            import systems.danger.kotlin.*

            // register plugin MyDangerPlugin

            danger(args) {
            
            }
        """