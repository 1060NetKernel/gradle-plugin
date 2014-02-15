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
    
    org.gradle.api.artifacts.Dependency dep(String name, String version='[1.0.0,)', String group='urn.com.ten60.core') {
        project.dependencies.create(group: group, name: name, version: version)
    }
    
    void useROCRepo() {
        project.repositories.maven { url "http://maven.netkernelroc.org:8080/netkernel-maven" }
    }
}
