package org.netkernel.gradle.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.netkernel.gradle.plugin.model.Module
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.nk.Download
import org.netkernel.gradle.plugin.util.FileSystemHelper

/**
 *  Manage configuration for the NetKernel plugin.
 *  TODO - Refactor into smaller pieces or separate calls from build.gradle with actual model
 */
class NetKernelExtension {

    enum SourceStructure {
        NETKERNEL, GRADLE
    }

    FileSystemHelper fileSystemHelper = new FileSystemHelper()
    Module module

    final Download download
//    final NamedDomainObjectContainer<ExecutionConfig> envs

    // NetKernel instances
    NamedDomainObjectContainer<NetKernelInstance> instances

    SourceStructure sourceStructure

    // Properties moved from primary plugin
    // TODO -  Move this to a properties file
    final String instanceName = 'SE'


    private Project project

    NetKernelExtension(Project project) {
        this.project = project
        this.download = new Download(project)
//        this.envs = envs
//        this.configName = configName
    }

    def download(Closure closure) {
        project.configure(download, closure)
    }

//    def envs(Closure closure) {
//        envs.configure(closure)
//    }

    def instances(Closure closure) {
        instances.configure(closure)
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
            provided dep('netkernel.api')
            provided dep('netkernel.impl')
            provided dep('layer0')
            provided dep('module.standard')
            provided dep('cache.se')
            provided dep('ext.layer1', '[1.0.0,)', 'urn.org.netkernel')
        }
    }

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
