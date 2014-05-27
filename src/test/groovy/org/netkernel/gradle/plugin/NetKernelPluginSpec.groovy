package org.netkernel.gradle.plugin

import org.gradle.api.InvalidUserDataException
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
        File projectDir = file("/examples/${projectDirName}")
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

    def 'uses version from module.xml'() {
        setup:
        File projectDir = file('/examples/basic_gradle_structure')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        when:
        netKernelPlugin.apply(project)

        then:
        project.version == '1.1.1'
        project.ext.nkModuleIdentity == "urn.org.netkernel.single.module-1.1.1"
    }

    def 'uses version from gradle project'() {
        setup:
        File projectDir = file('/examples/basic_gradle_structure')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        Closure taskDependency = super.assertTaskDependencyClosure.curry(project)
        project.version = "1.0.0"

        when:
        netKernelPlugin.apply(project)

        then:
        project.version == '1.0.0'
        project.ext.nkModuleIdentity == 'urn.org.netkernel.single.module-1.0.0'

        // Make sure that update module xml task was created and added into the dependency chain
        project.tasks.getByName('updateModuleXmlVersion') != null
        taskDependency('updateModuleXmlVersion', 'moduleResources')
        taskDependency('jar', 'updateModuleXmlVersion')
    }

    def 'fails if no module xml is found'() {
        setup:
        File projectDir = file('/examples/module_missing_module_xml')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        when:
        netKernelPlugin.apply(project)

        then:
        thrown(InvalidUserDataException)
    }

}
