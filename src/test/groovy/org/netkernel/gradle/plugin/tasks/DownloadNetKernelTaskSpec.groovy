package org.netkernel.gradle.plugin.tasks

import org.gradle.api.Project
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.Release
import org.netkernel.gradle.plugin.nk.DownloadConfig
import org.netkernel.gradle.plugin.util.FileSystemHelper
import org.netkernel.gradle.plugin.util.PropertyHelper
import spock.lang.Ignore

class DownloadNetKernelTaskSpec extends BasePluginSpec {

    DownloadNetKernelTask downloadNetKernelTask
    FileSystemHelper mockFileSystemHelper = Mock()
    PropertyHelper mockPropertyHelper = Mock()
    DownloadConfig downloadConfig

    void setup() {
        mockFileSystemHelper = Mock()
        mockPropertyHelper = Mock()
        downloadConfig = new DownloadConfig()

        downloadNetKernelTask = createTask(DownloadNetKernelTask)
        downloadNetKernelTask.fileSystemHelper = mockFileSystemHelper
        downloadNetKernelTask.propertyHelper = mockPropertyHelper
        downloadNetKernelTask.downloadConfig = downloadConfig

    }

    @Ignore
    def 'downloads NetKernel'() {
        setup:
        File downloadDirectory = getResourceAsFile('/test/gradleHomeDirectory/netkernel/download')
        File distributionDir = getResourceAsFile('/test/distributions')
        downloadNetKernelTask.release = new Release(edition)
        downloadNetKernelTask.destinationFile = file '/test/gradleHomeDirectory/netkernel/download', resultantJarFileName
        downloadConfig.url = distributionDir.toURI().toURL()
        downloadConfig.username = 'username'
        downloadConfig.password = 'password'

        when:
        downloadNetKernelTask.downloadNetKernel()

        then:
        (0..1) * mockPropertyHelper.findProjectProperty(_ as Project, 'nkeeUsername', 'username') >> 'username'
        (0..1) * mockPropertyHelper.findProjectProperty(_ as Project, 'nkeePassword', 'password') >> 'password'
        new File(downloadDirectory, resultantJarFileName).exists()

        where:
        edition            | resultantJarFileName
        Edition.STANDARD   | '1060-NetKernel-SE-5.2.1.jar'
        Edition.ENTERPRISE | '1060-NetKernel-EE-5.2.1.jar'
    }

}
