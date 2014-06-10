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
    PropertyHelper propertyHelper = new PropertyHelper()

    final Download download

    // This should probably be a list or else this model operates on a single module at a time
    Module module

    // NetKernel instances (SE & EE come for free)
    NamedDomainObjectContainer<NetKernelInstance> instances

    SourceStructure sourceStructure

    Project project

    NetKernelExtension(Project project) {
        this.project = project
        this.download = new Download(project)
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

    void useROCRepo() {
        useRepo propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_NETKERNELROC_URL)
    }

    void useLocalhostRepo() {
        useRepo propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_LOCAL_URL)
    }

    void useNKRepo() {
        useRepo propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_NETKERNEL_URL)
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
        return workFile('freeze')
    }

    File getDestinationDirectory() {
        return workFile('')
    }

    File getThawDirectory() {
        return workFile('thaw')
    }

    File getThawInstallationDirectory() {
        return workFile('thawInstallation')
    }

    File getFrozenArchiveFile() {
        return workFile('download/frozen.zip')
    }

    /**
     * Returns a file reference that is inside the ~/.gradle/netkernel directory.
     *
     * @param location location of file or directory
     *
     * @return file reference
     */
    File workFile(String location) {
        return fileSystemHelper.fileInGradleHome('netkernel/' + location)
    }

    String currentMajorReleaseVersion() {
        return propertyHelper.findProjectProperty(project, PropertyHelper.CURRENT_MAJOR_RELEASE_VERSION, null)
    }

    String distributionJarFile(Release release) {
        return propertyHelper.findProjectProperty(project, PropertyHelper.DISTRIBUTION_JAR_NAME, null, [edition: release.edition, version: release.version])
    }

    String getInstanceName() {
        return propertyHelper.findProjectProperty(project, PropertyHelper.NETKERNEL_INSTANCE, 'SE')
    }

    String projectProperty(String propertyName, String defaultValue = null, Map values = null) {
        return propertyHelper.findProjectProperty(project, propertyName, defaultValue, values)
    }

}
