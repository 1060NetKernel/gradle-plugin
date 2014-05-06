package org.netkernel.gradle.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.netkernel.gradle.nk.ExecutionConfig

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

    org.gradle.api.artifacts.Dependency dep(String name, String version = '[1.0.0,)', String group = 'urn.com.ten60.core') {
        project.dependencies.create(group: group, name: name, version: version)
    }

    void useROCRepo() {
        useRepo "http://maven.netkernelroc.org:8080/netkernel-maven"
    }

    void useLocalhostRepo() {
        useRepo "http://localhost:8080/netkernel-maven"
    }

    void useRepo(String repoURL) {
        project.repositories.maven { url repoURL }
    }

    void useMavenCentral() {
        project.repositories.mavenCentral()
    }

    void useStandardCompileDependencies() {
        project.dependencies {
            compile dep('netkernel.api')
            compile dep('netkernel.impl')
            compile dep('layer0')
            compile dep('module.standard')
            compile dep('cache.se')
            compile dep('ext.layer1', '[1.0.0,)', 'urn.org.netkernel')
        }
    }


}
