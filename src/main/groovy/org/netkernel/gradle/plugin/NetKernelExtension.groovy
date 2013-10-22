package org.netkernel.gradle.plugin

import org.gradle.api.Project

/**
 *  Manage configuration for the NetKernel plugin.
 */
class NetKernelExtension {

    final Download download
    private Project project

    NetKernelExtension(Project project) {
        this.project = project
        this.download = new Download(project)
    }

    def download(Closure closure) {
        project.configure(download, closure)
    }
}
