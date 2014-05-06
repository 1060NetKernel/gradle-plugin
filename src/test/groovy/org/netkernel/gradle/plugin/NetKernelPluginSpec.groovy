package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class NetKernelPluginSpec extends BasePluginSpec {

    NetKernelPlugin netKernelPlugin
    Set<String> providedTaskNames = [
        'copyBeforeFreeze',
        'createAppositePackage',
        'downloadNKEE',
        'downloadNKSE',
        'freezeDelete',
        'freezeJar',
        'freezeTidy',
        'installFreeze',
        'module',
        'moduleResources',
        'uploadFreeze'
    ] as Set

    void setup() {
        netKernelPlugin = new NetKernelPlugin()
    }

    def 'applies NetKernel plugin to basic projects'() {
        setup:
        File projectDir = new File(NetKernelPluginSpec.getResource(projectDirPath).file)
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        Closure taskDependency = super.assertTaskDependencyClosure.curry(project)

        when:
        netKernelPlugin.apply(project)

        then:
        providedTaskNames.each { name ->
            assert project.tasks.findByName(name) != null
        }

        // Assert task dependencies
        taskDependency('moduleResources', 'module')
        taskDependency('module', 'compileGroovy')
        taskDependency('jar', 'moduleResources')
        taskDependency('freezeTidy', 'copyBeforeFreeze')
        taskDependency('freezeJar', 'freezeTidy')
        taskDependency('freezeDelete', 'freezeJar')
        taskDependency('thawExpand', 'thawDeleteInstall')
        taskDependency('thawConfigure', 'thawExpand')

        // Assert added configurations
        project.configurations.getByName('freeze') != null
        project.configurations.getByName('thaw') != null

        where:
        projectDirPath << ['/modules/basic_gradle_structure', '/modules/basic_netkernel_structure']
    }


}
