package org.netkernel.gradle.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 *  Manage configuration for the NetKernel plugin.
 */
class NetKernelExtension {

    final Download download
    final NamedDomainObjectContainer<ExecutionConfig> envs

    private Project project

    NetKernelExtension(Project project, envs) {
        this.project = project
        this.download = new Download(project)
        this.envs = envs
    }

    def download(Closure closure) {
        project.configure(download, closure)
    }

    def envs(Closure closure) {
        envs.configure(closure)
    }
}
