package org.netkernel.gradle.plugin

import org.gradle.api.Project

/**
 *  A simple class to manage NetKernel download parameters.
 */
class Download {
    final DownloadConfig ee = new DownloadConfig()
    final DownloadConfig se = new DownloadConfig()
    final Project project

    Download(Project project) {
        this.project = project
    }

    def ee(Closure closure) {
        project.configure(ee, closure)
    }

    def se(Closure closure) {
        project.configure(se, closure)
    }
}
