package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    void apply(Project project) {

        def extension = project.extensions.create("netkernel", NetKernelExtension, project)

        project.task('downloadNKSE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.se
        }

        project.task('downloadNKEE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }
    }
}
