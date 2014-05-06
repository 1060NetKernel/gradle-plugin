package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NetKernelPluginSpec extends Specification {

    NetKernelPlugin netKernelPlugin

    void setup() {
        netKernelPlugin = new NetKernelPlugin()
    }

    def 'applies NetKernel plugin to project'() {
        setup:
        File projectDir = new File(NetKernelPluginSpec.getResource("/modules/basic").file)
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        when:
        netKernelPlugin.apply(project)

        then:
        project
    }

}
