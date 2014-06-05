package org.netkernel.gradle.plugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.NetKernelExtension
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.Release
import org.netkernel.gradle.plugin.model.SourceStructure
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask
import org.netkernel.gradle.plugin.tasks.TaskName
import spock.lang.Unroll

class NetKernelPluginSpec extends BasePluginSpec {

    NetKernelPlugin netKernelPlugin
    Set<String> providedTaskNames = [
        'copyBeforeFreeze',
        'createAppositePackage',
        'downloadEE',
        'downloadSE',
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
        Closure assertTaskDependency = super.assertTaskDependencyClosure.curry(project)

        when:
        netKernelPlugin.apply(project)

        then:
        // A fair amount of assertions here to check project
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
        project.configurations.getByName('provided') != null

        // Assertions on model created
        project.extensions.getByName('netkernel') != null
        NetKernelExtension extension = project.extensions.getByName('netkernel')
        extension.sourceStructure == expectedSourceStructure
        extension.module != null

        where:
        projectDirName               | expectedSourceStructure
        'basic_gradle_structure'     | SourceStructure.GRADLE
        'basic_netkernel_structure'  | SourceStructure.NETKERNEL
        '01-single-module'           | SourceStructure.GRADLE
        '02-nkjava-module'           | SourceStructure.NETKERNEL
        '03-nkjava-module'           | SourceStructure.NETKERNEL
        '04-module-mavendep'         | SourceStructure.GRADLE
        '05-module-moduledep'        | SourceStructure.GRADLE
        '06-module-mavenexternaljar' | SourceStructure.GRADLE
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

    def 'download SE configured'() {
        setup:
        Project project = project '/examples/basic_gradle_structure'
        netKernelPlugin.apply(project)
        DownloadNetKernelTask downloadNetKernelTask = project.tasks.getByName(TaskName.DOWNLOAD_SE)

        expect:
        downloadNetKernelTask.destinationFile != null
    }

    def 'creates single netkernel instance reference'() {
        setup:
        File location = file '/test/NetKernelPluginSpec/install/EE-5.2.1'
        File jarFileLocation = file '/test/NetKernelPluginSpec/1060-NetKernel-SE-5.2.1.jar'
        Edition edition = Edition.STANDARD
        String version = Release.CURRENT_MAJOR_RELEASE

        when:
        NetKernelInstance instance = netKernelPlugin.createNetKernelInstance(edition, location, jarFileLocation)

        then:
        instance.name == 'SE'
        instance.release.version == version
        instance.release.edition == edition
        instance.url == new URL('http://localhost')
        instance.backendPort == 1060
        instance.frontendPort == 8080
        instance.location == location
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

    def 'creates netkernel instance tasks for SE Edition'() {
        setup:
        netKernelPlugin.project = project
        File archiveFile = file '/test/NetKernelPluginSpec/module.jar'
        project.tasks.create(name: 'jar', type: Jar)

        NetKernelInstance netKernelInstance = new NetKernelInstance(
            name: 'SE',
            release: new Release(Edition.STANDARD),
            location: file('/test/NetKernelPluginSpec/install/SE-5.2.1'),
            jarFileLocation: file('/test/NetKernelPluginSpec/1060-NetKernel-SE-5.2.1.jar')
        )

        Set<String> taskNames = ['startSE', 'stopSE', 'installSE', 'deployToSE', 'undeployFromSE']

        when:
        netKernelPlugin.createNetKernelInstanceTasks(netKernelInstance)

        then:
        taskNames.each { taskName ->
            assert project.tasks.findByName(taskName) != null
            assert project.tasks.getByName(taskName).netKernelInstance == netKernelInstance
        }

        and:
        project.tasks.findAll { it.name.contains('deploy') }.each { Task task ->
            assert task.moduleArchiveFile != null
        }
    }

    def 'creates netkernel instance tasks for EE & SE Edition'() {
        setup:
        createNetKernelExtension()
        netKernelPlugin.project = project
        project.tasks.create(name: 'jar', type: Jar)
        // A fancy groovy way of building list of task names
        List<String> taskNames = [
            ['start', 'stop', 'install', 'deployTo', 'undeployFrom'],
            ['SE', 'EE']
        ].combinations().collect({ l -> "${l[0]}${l[1]}" as String })
        project.extensions.getByName('netkernel').instances = netKernelPlugin.createNetKernelInstances()
        netKernelPlugin.netKernel = project.extensions.getByName('netkernel')

        when:
        netKernelPlugin.createNetKernelInstanceTasks()

        then:
        taskNames.each { String name ->
            assert project.tasks.findByName(name) != null
        }
    }

}
