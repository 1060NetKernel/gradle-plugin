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

    //String[] packageList

    NetKernelExtension(Project project) {
        this.project = project
        this.download = new Download(project)
    }

    def download(Closure closure) {
        project.configure(download, closure)
    }

    def instances(Closure closure) {
        instances.configure(closure)

        // After initial configuration, loop through instances and fill in default values if missing
        instances.each { NetKernelInstance instance ->
            instance.edition = instance.edition ?: Edition.STANDARD
            instance.netKernelVersion = instance.netKernelVersion ?: currentMajorReleaseVersion()
            instance.url = instance.url ?: new URL(projectProperty(PropertyHelper.NETKERNEL_INSTANCE_DEFAULT_URL))
            instance.backendPort = instance.backendPort ?: projectProperty(PropertyHelper.NETKERNEL_INSTANCE_BACKEND_PORT) as int
            instance.frontendPort = instance.frontendPort ?: projectProperty(PropertyHelper.NETKERNEL_INSTANCE_FRONTEND_PORT) as int
            instance.jarFileLocation = instance.jarFileLocation ?:
                workFile(projectProperty(PropertyHelper.NETKERNEL_INSTANCE_DOWNLOAD_JAR_NAME, null, [edition: instance.edition, netKernelVersion: instance.netKernelVersion]))
        }
    }


//    String distributionJarFile(Edition edition, String netKernelVersion) {
//        return propertyHelper.findProjectProperty(project, PropertyHelper.DISTRIBUTION_JAR_NAME, null, [edition: edition, netKernelVersion: netKernelVersion])
//    }

    Dependency dependency(String name, String version = '[1.0.0,)', String group = 'urn.com.ten60.core') {
        project.dependencies.create(group: group, name: name, version: version)
    }

    void useROCRepo() {
        //useRepo propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_NETKERNELROC_URL)
        throw new Exception("NK-ROC Repo is now offline. Please use the official 1060 NetKernel maven repository with .useNKRepo()");
    }

    void useMavenNKLocal() {
        useMaven propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_LOCAL_URL)
    }

    void useMavenNK() {
        println("******Added Official NetKernel Repo********")
        useMaven propertyHelper.findProjectProperty(project, PropertyHelper.MAVEN_NETKERNEL_URL)
    }

    void useMaven(String repoURL) {
        println("Adding repo: ${repoURL}")
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

    String distributionJarFile(Edition edition, String netKernelVersion) {
        return propertyHelper.findProjectProperty(project, PropertyHelper.DISTRIBUTION_JAR_NAME, null, [edition: edition, netKernelVersion: netKernelVersion])
    }

    String getInstanceName() {
        return propertyHelper.findProjectProperty(project, PropertyHelper.NETKERNEL_INSTANCE, 'SE')
    }

    String projectProperty(String propertyName, String defaultValue = null, Map values = null) {
        return propertyHelper.findProjectProperty(project, propertyName, defaultValue, values)
    }

}
