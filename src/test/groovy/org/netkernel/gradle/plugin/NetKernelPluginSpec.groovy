package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Unroll

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

    @Unroll
    def 'applies NetKernel plugin to sample projects #projectDirName'() {
        setup:
        File projectDir = new File(NetKernelPluginSpec.getResource("/modules/${projectDirName}").file)
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
        projectDirName << [
            'basic_gradle_structure',
            'basic_netkernel_structure',
            '01-single-module',
            '02-nkjava-module',
            '03-nkjava-module',
            '04-module-mavendep',
            '05-module-moduledep',
            '06-module-mavenexternaljar'
        ]
    }


}
