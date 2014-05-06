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
        project.tasks.findAll { task ->
            ['listTemplates','createModule'].contains(task.name)
        }.size() == 2
    }

}
