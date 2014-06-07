package org.netkernel.gradle.plugin.tasks

import org.gradle.api.Project
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.DownloadConfig
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.PropertyHelper
import org.netkernel.gradle.plugin.model.Release
import spock.lang.Ignore

class DownloadNetKernelTaskSpec extends BasePluginSpec {

    DownloadNetKernelTask downloadNetKernelTask
    PropertyHelper mockPropertyHelper = Mock()
    DownloadConfig downloadConfig

    void setup() {
        mockPropertyHelper = Mock()
        downloadConfig = new DownloadConfig()

        downloadNetKernelTask = createTask(DownloadNetKernelTask)
        downloadNetKernelTask.propertyHelper = mockPropertyHelper
        downloadNetKernelTask.downloadConfig = downloadConfig
    }

    @Ignore('Still working on this one...')
    def 'downloads NetKernel'() {
        setup:
        File downloadDirectory = getResourceAsFile('/test/gradleHomeDirectory/netkernel/download')
        File distributionDir = getResourceAsFile('/test/distributions')
        downloadNetKernelTask.release = new Release(edition: edition, version: '5.2.1')
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
