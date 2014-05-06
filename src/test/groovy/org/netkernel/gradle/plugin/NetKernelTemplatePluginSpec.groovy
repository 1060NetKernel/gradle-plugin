package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NetKernelTemplatePluginSpec extends Specification {

    Project project
    NetKernelTemplatePlugin netKernelTemplatePlugin

    void setup() {
        project = ProjectBuilder.builder().build()
        netKernelTemplatePlugin = new NetKernelTemplatePlugin()
    }

    def 'creates template tasks'() {
        when:
        netKernelTemplatePlugin.apply(project)

        then:
        project.configurations.getByName('templates') != null
        project.tasks.findByName('listTemplates') != null
        project.tasks.findByName('createModule') != null
    }

}
