package org.netkernel.gradle.plugin.model

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.netkernel.gradle.plugin.util.FileSystemHelper

/**
 *  Manage configuration for the NetKernel plugin.
 *  TODO - Refactor into smaller pieces or separate calls from build.gradle with actual model
 */
class NetKernelExtension {

    FileSystemHelper fileSystemHelper = new FileSystemHelper()

    final Download download
    // Properties moved from primary plugin
    // TODO -  Move this to a properties file
    final String instanceName = 'SE'
    Module module

    // NetKernel instances (SE & EE come for free)
    NamedDomainObjectContainer<NetKernelInstance> instances

    SourceStructure sourceStructure

    Project project

    NetKernelExtension(Project project) {
        this.project = project
        this.download = new Download(project)
        project.setProperty('hello', 'nick')
    }

    def download(Closure closure) {
        project.configure(download, closure)
    }

    def instances(Closure closure) {
        instances.configure(closure)
    }

    Dependency dependency(String name, String version = '[1.0.0,)', String group = 'urn.com.ten60.core') {
        project.dependencies.create(group: group, name: name, version: version)
    }

    // TODO - Move to properties file
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
            provided dependency('netkernel.api')
            provided dependency('netkernel.impl')
            provided dependency('layer0')
            provided dependency('module.standard')
            provided dependency('cache.se')
            provided dependency('ext.layer1', '[1.0.0,)', 'urn.org.netkernel')
        }
    }

    /**
     * TODO - Are we sure this is the installationDirectory we want, what about EE or SE?
     * @return
     */
    File getInstallationDirectory() {
        return instances[instanceName].location
    }

    File getFreezeDirectory() {
        return fileSystemHelper.fileInGradleHome('netkernel/freeze')
    }

    File getDestinationDirectory() {
        return fileSystemHelper.fileInGradleHome('netkernel')
    }

    File getThawDirectory() {
        return fileSystemHelper.fileInGradleHome('netkernel/thaw')
    }

    File getThawInstallationDirectory() {
        return fileSystemHelper.fileInGradleHome('netkernel/thawInstallation')
    }

    File getFrozenArchiveFile() {
        return fileSystemHelper.fileInGradleHome('netkernel/download/frozen.zip')
    }

}
