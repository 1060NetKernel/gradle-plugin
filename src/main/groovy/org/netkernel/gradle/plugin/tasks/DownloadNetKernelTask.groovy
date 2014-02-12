package org.netkernel.gradle.plugin.tasks
import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.BuildListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.DownloadConfig
import org.netkernel.gradle.util.FileSystemHelper
/*
 * A task to download a version of NetKernel.
 */
class DownloadNetKernelTask extends DefaultTask {
    String release = NKSE
    String version = '5.2.1'
    //String version = CURRENT_MAJOR_NK_RELEASE  //Don't know how to get this from the NetKernelPlugin class
    String baseURL = DISTRIBUTION_URL
    String releaseDir
    String filePrefix

    DownloadConfig downloadConfig

    def fsHelper = new FileSystemHelper()

    //TODO: Drive some of this from the ExecutionConfigs?

    // Defaults
    static def DISTRIBUTION_URL = 'http://apposite.netkernel.org/dist'
    static def NKSE = 'SE'
    static def NKEE = 'EE'
    static def DEFAULT_RELEASEDIRS = [ 'SE' : '1060-NetKernel-SE',
                                       'EE' : '1060-NetKernel-EE' ]

    @TaskAction
    void downloadNetKernel() {
        def dest = fsHelper.dirInGradleHomeDirectory("netkernel/download")
        if(!fsHelper.dirExists(dest)&&!fsHelper.createDirectory(dest)) {
            // TODO: Handle this exception
            println "Error creating: ${dest}"
        }

        def url

        if(downloadConfig.url != null) {
            baseURL = downloadConfig.url
        }

        switch(release) {
            case NKSE:
            case NKEE:
                if(releaseDir == null) {
                    releaseDir = DEFAULT_RELEASEDIRS[release]
                }

                if(filePrefix == null) {
                    filePrefix = DEFAULT_RELEASEDIRS[release]
                }

                url = "${baseURL}/${releaseDir}/${filePrefix}-${version}.jar"
            break;
            default:
                // TODO: Fail, unknown version
            break;
        }

        println "Downloading ${url} to ${dest}"

        ant.project.buildListeners.toList().each {
            ant.project.removeBuildListener(it)
        }

        ant.project.addBuildListener(new BuildListener() {
            void buildStarted(BuildEvent event) {}
            void buildFinished(BuildEvent event) {}
            void targetStarted(BuildEvent event) {}
            void targetFinished(BuildEvent event) {}
            void taskStarted(BuildEvent event) {}
            void taskFinished(BuildEvent event) {}

            void messageLogged(BuildEvent event) {
                DownloadNetKernelTask.this.logger.quiet event.message
            }
        })

        ant.get(src: url,
                dest: dest,
                verbose: true,
                httpusecaches : true,
                usetimestamp : true)
    }
}
