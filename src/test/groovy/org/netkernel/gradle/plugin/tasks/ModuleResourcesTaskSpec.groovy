package org.netkernel.gradle.plugin.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.NetKernelPlugin

class ModuleResourcesTaskSpec extends BasePluginSpec {

    def 'processes module resources'() {
        setup:
        File projectDir = file('/examples/basic_gradle_structure')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        NetKernelPlugin netKernelPlugin = new NetKernelPlugin()
        netKernelPlugin.apply(project)
        project.apply(plugin: 'groovy')
        ModuleResourcesTask moduleResourcesTask = project.tasks.getByName('moduleResources')

        project.dependencies.add('provided', project.files(ModuleResourcesTaskSpec.getResource('/test/files/sample-provided-dependency.jar').file))
        project.dependencies.add('compile', project.files(ModuleResourcesTaskSpec.getResource('/test/files/sample-compile-dependency.jar').file))

        when:
        moduleResourcesTask.processModuleResources()

        then:
        File libDir = new File("${project.buildDir}/${project.extensions.netkernel.module.name}/lib")
        libDir.listFiles().size() == 1
        libDir.listFiles().find { it.name == "sample-compile-dependency.jar" }
    }

}
