package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask
import org.netkernel.gradle.plugin.tasks.InstallNetKernelTask
import org.netkernel.gradle.plugin.tasks.StartNetKernelTask
import org.netkernel.gradle.util.FileSystemHelper

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def CURRENT_MAJOR_NK_RELEASE = '5.1.1'

    void apply(Project project) {

        def envs = project.container(ExecutionConfig)

        def defaultSEJar = new ExecutionConfig()

        project.configure(defaultSEJar, {
            release = CURRENT_MAJOR_NK_RELEASE
            installJar = "1060-NetKernel-SE-${release}.jar"
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/SE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })

        def defaultEEJar = new ExecutionConfig()

        project.configure(defaultEEJar,  {
            release = CURRENT_MAJOR_NK_RELEASE
            installJar = "1060-NetKernel-EE-${release}.jar"
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/EE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })

        def extension = project.extensions.create("netkernel", NetKernelExtension, project, envs)

        project.task('downloadNKSE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.se
        }

        project.task('downloadNKEE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }

        // Default SE Behavior

        project.task('startNKSE', type: StartNetKernelTask) {
            executionConfig = defaultSEJar
        }

        project.tasks.startNKSE.dependsOn "downloadNKSE"
        project.task('installNKSE', type: InstallNetKernelTask) {
            executionConfig = defaultSEJar
        }
        project.tasks.installNKSE.dependsOn "startNKSE"

        // Default EE Behavior

        project.task('startNKEE', type: StartNetKernelTask) {
            executionConfig = defaultEEJar
        }

        project.tasks.startNKEE.dependsOn "downloadNKEE"
        project.task('installNKEE', type: InstallNetKernelTask) {
            executionConfig = defaultEEJar
        }
        project.tasks.installNKEE.dependsOn "startNKEE"

        // TODO: Add the above behavior for every environment
    }
}
