package org.netkernel.gradle.plugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.Release

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

    def 'applies NetKernel plugin to sample projects #projectDirName'() {
        setup:
        File projectDir = file("/examples/${projectDirName}")
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        Closure assertTaskDependency = super.assertTaskDependencyClosure.curry(project)

        when:
        netKernelPlugin.apply(project)

        then:
        providedTaskNames.each { name ->
            assert project.tasks.findByName(name) != null
        }

        // Assert task dependencies
        assertTaskDependency('moduleResources', 'module')
        assertTaskDependency('module', 'compileGroovy')
        assertTaskDependency('jar', 'moduleResources')
        assertTaskDependency('freezeTidy', 'copyBeforeFreeze')
        assertTaskDependency('freezeJar', 'freezeTidy')
        assertTaskDependency('freezeDelete', 'freezeJar')
        assertTaskDependency('thawExpand', 'thawDeleteInstall')
        assertTaskDependency('thawConfigure', 'thawExpand')

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
        project.extensions.netkernel.module.name == "urn.org.netkernel.single.module-1.1.1"
    }

    def 'uses version from gradle project'() {
        setup:
        File projectDir = file('/examples/basic_gradle_structure')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        Closure assertTaskDependency = super.assertTaskDependencyClosure.curry(project)
        project.version = "1.0.0"

        when:
        netKernelPlugin.apply(project)

        then:
        project.version == '1.0.0'
        project.extensions.netkernel.module.name == 'urn.org.netkernel.single.module-1.0.0'

        // Make sure that update module xml task was created and added into the dependency chain
        project.tasks.getByName('updateModuleXmlVersion') != null
        assertTaskDependency('updateModuleXmlVersion', 'moduleResources')
        assertTaskDependency('jar', 'updateModuleXmlVersion')
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

    def 'copyBeforeFreeze task initialized properly'() {
        setup:
        File projectDir = file('/examples/basic_gradle_structure')
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        netKernelPlugin.apply(project)

        when:
        Copy copyBeforeFreeze = project.tasks.getByName('copyBeforeFreeze')

        then:
        copyBeforeFreeze.getIncludes() == ['**/*'] as Set
    }

    def 'creates single netkernel instance reference'() {
        setup:
        File location = file path
        Edition edition = Edition.STANDARD
        String version = Release.CURRENT_MAJOR_RELEASE

        when:
        NetKernelInstance instance = netKernelPlugin.createNetKernelInstance(edition, location)

        then:
        instance.name == name
        instance.release.version == version
        instance.release.edition == edition
        instance.url == new URL('http://localhost')
        instance.backendPort == 1060
        instance.frontendPort == 8080
        instance.location == location

        where:
        path                                                    | name
        '/test/NetKernelPluginSpec/1060-NetKernel-SE-5.2.1.jar' | 'SEjar'
        '/test/NetKernelPluginSpec/install/EE-5.2.1'            | 'SE'
    }

    def 'creates netkernel instance references'() {
        setup:
        netKernelPlugin.project = project

        when:
        NamedDomainObjectContainer<NetKernelInstance> instances = netKernelPlugin.createNetKernelInstances()

        then:
        instances != null
        instances['SE'] != null
    }

    def 'creates netkernel instance tasks'() {
        setup:
        netKernelPlugin.project = project

        NetKernelInstance netKernelInstance = new NetKernelInstance(
            name: 'test',
            location: file(location),
            installationDirectory: file(installationDirectory)
        )

        when:
        netKernelPlugin.createNetKernelInstanceTasks(netKernelInstance)

        then:
        taskNames.each { taskName ->
            assert project.tasks.findByName(taskName) != null
            assert project.tasks.getByName(taskName).netKernelInstance == netKernelInstance
        }

        where:
        location                                                | installationDirectory                        | taskNames
        '/test/NetKernelPluginSpec/1060-NetKernel-SE-5.2.1.jar' | '/test/NetKernelPluginSpec/install/SE-5.2.1' | ['starttest', 'stoptest', 'installtest']
        '/test/NetKernelPluginSpec/install/EE-5.2.1'            | null                                         | ['starttest', 'stoptest']
    }

}
