package org.netkernel.gradle.plugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.Module
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.Release
import org.netkernel.gradle.plugin.tasks.ConfigureAppositeTask
import org.netkernel.gradle.plugin.tasks.CreateAppositePackageTask
import org.netkernel.gradle.plugin.tasks.DeployToNetKernelTask
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask
import org.netkernel.gradle.plugin.tasks.FreezeTidyTask
import org.netkernel.gradle.plugin.tasks.InstallNetKernelTask
import org.netkernel.gradle.plugin.tasks.ModuleResourcesTask
import org.netkernel.gradle.plugin.tasks.StartNetKernelTask
import org.netkernel.gradle.plugin.tasks.StopNetKernelTask
import org.netkernel.gradle.plugin.tasks.ThawConfigureTask
import org.netkernel.gradle.plugin.tasks.UndeployFromNetKernelTask
import org.netkernel.gradle.plugin.tasks.UpdateModuleXmlVersionTask
import org.netkernel.gradle.plugin.util.FileSystemHelper

import static org.netkernel.gradle.plugin.tasks.TaskName.*

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {

    def fsHelper = new FileSystemHelper()

    Project project
    NetKernelExtension netKernel

    void apply(Project project) {
        this.project = project

        // TODO - Do we always want to apply the groovy plugin?  What about other languages like kotlin, scala, etc.?
        project.apply plugin: 'groovy'
        project.apply plugin: 'maven'

        configureProject()
        createTasks()
        configureTasks()
        createTaskDependencies()
        afterEvaluate()
    }

    /**
     * Initial configuration for the plugin.  This is where the backing model is initialized
     * and configurations are added.  Also determined is the source structure of the project,
     * be it NetKernel or gradle.
     */
    void configureProject() {
        ['freeze', 'thaw', 'provided'].each { name ->
            project.configurations.create(name)
        }
        project.configurations.compile.extendsFrom(project.configurations.provided)

        netKernel = project.extensions.create("netkernel", NetKernelExtension, project)
        netKernel.instances = createNetKernelInstances()

        if (new File(project.projectDir, "src/module.xml").exists()) {
            netKernel.sourceStructure = NetKernelExtension.SourceStructure.NETKERNEL
        } else {
            netKernel.sourceStructure = NetKernelExtension.SourceStructure.GRADLE
        }

        // TODO - I don't know what this does
        //        project.artifacts {
//            freeze project.tasks[FREEZE_JAR].outputs
//        }

        switch (netKernel.sourceStructure) {
            case NetKernelExtension.SourceStructure.NETKERNEL:
                def baseDir = new File(project.projectDir, 'src/')
                //Configure the javaCompiler
                def fileTree = project.fileTree(dir: baseDir, includes: ['**/*.java'])
                //fileTree.visit { f ->  println f }
                project.tasks.compileJava.configure {
                    source = fileTree
                }
                //Configure the groovyCompiler
                fileTree = project.fileTree(dir: new File(project.projectDir, 'src/'), includes: ['**/*.groovy'])
                project.tasks.compileGroovy.configure {
                    source = fileTree
                }
                netKernel.module = new Module(project.file('src/module.xml'))

                //Add any libs to classpath
                def libDir = new File(baseDir, "lib/")
                if (libDir.exists()) {
                    def libTree = project.fileTree(dir: libDir, includes: ['**/*.jar'])
                    libTree.visit { f ->
                        //println "lib/ DEPENDENCY ADDED: ${f}"
                    }
                    project.dependencies.add("compile", libTree)
                }

                break;
            case NetKernelExtension.SourceStructure.GRADLE:
                if (project.file('src/module/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/module/module.xml'))
                }
                if (project.file('src/main/resources/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/main/resources/module.xml'))
                }
                break;
        }

        if (!netKernel.module) {
            throw new InvalidUserDataException("Could not find module.xml in the project.")
        }

        // If the project has a version specified, override the value in the module.xml
        if (project.version == 'unspecified') {
            project.version = netKernel.module.version
        } else {
            netKernel.module.version = project.version
        }

        //Set Maven Artifact name and version
        //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
        project.archivesBaseName = netKernel.module.URIDotted
    }

    /**
     * Creates all of the tasks for this plugin.  Some tasks may be disabled depending on
     * what is provided by the build.gradle file.
     */
    void createTasks() {

//        createTask(CLEAN_SE_JAR, CleanDownloadedJarTask, 'Removes downloaded SE jar')

        createTask(CONFIGURE_APPOSITE, ConfigureAppositeTask, 'Configures apposite repository')

        createTask(COPY_BEFORE_FREEZE, Copy, 'Copies NetKernel installation into freeze directory')

        createTask(CREATE_APPOSITE_PACKAGE, CreateAppositePackageTask, 'Creates apposite package')

        createTask(DOWNLOAD_EE, DownloadNetKernelTask, 'Downloads NetKernel EE edition')

        createTask(DOWNLOAD_SE, DownloadNetKernelTask, 'Downloads NetKernel SE edition')

        createTask(FREEZE_DELETE, Delete, "Deletes frozen NetKernel instance")

        createTask(FREEZE_JAR, Jar, 'Creates zip file of frozen NetKernel instance')

        createTask(FREEZE_TIDY, FreezeTidyTask, 'Cleans up copied NetKernel instance during freeze')

        createTask(INSTALL_FREEZE, Upload, "Installs frozen NetKernel instance into maven repository")

        createTask(MODULE, Copy, "Copies built classes to build folder")

        createTask(MODULE_RESOURCES, ModuleResourcesTask, 'Copies module resources (module.xml, etc.) to build folder')

        createTask(THAW, Copy, "Copies frozen NetKernel instance into thaw directory")
            .setEnabled(project.configurations.thaw.files.size() == 1)

        createTask(THAW_CONFIGURE, ThawConfigureTask, "Thaws configuration for NetKernel instance")

        createTask(THAW_DELETE_INSTALL, Delete, "Deletes thawed installation directory")

        createTask(THAW_EXPAND, Copy, "Expands thawed NetKernel instance into thaw installation directory")

        createTask(UPDATE_MODULE_XML_VERSION, UpdateModuleXmlVersionTask, 'Updates version in module xml to match project version')
            .setEnabled(netKernel.module.versionOverridden)

        /*        project.task('freezePublish', type: org.gradle.api.publish.maven.tasks.PublishToMavenLocal) {
            publication {
                from freezeDir
                artifact 'frozen.zip'
            }
        } */

    }

    /**
     * Any custom configuration for the tasks is done here.  Specific data needed by the tasks
     * is provided by the backing model classes (e.g. {@see NetKernelExtension})
     */
    void configureTasks() {

        configureTask(COPY_BEFORE_FREEZE) {
            from netKernel.installationDirectory
            into netKernel.freezeDirectory
            include "**/*"
        }

        configureTask(FREEZE_TIDY) {
            freezeDirInner netKernel.freezeDirectory
            installDirInner netKernel.installationDirectory
        }

        configureTask(FREEZE_JAR) {
            from netKernel.freezeDirectory
            destinationDir = netKernel.destinationDirectory
            archiveName = 'frozen.zip'
        }

        configureTask(INSTALL_FREEZE) {
            // TODO - Introduce constants for configuration 'freeze', etc.
            configuration = project.configurations.freeze
            repositories {
                mavenInstaller()
            }
            repositories.withType(MavenResolver) {
                pom.groupId = 'org.netkernel'
                pom.version = '1.1.1'
                pom.artifactId = 'frozenInstance'
                pom.scopeMappings.mappings.clear()
            }
        }

        if (project.tasks[THAW].enabled) {
            configureTask(THAW) {
                into netKernel.thawDirectory
                from project.zipTree(project.configurations.thaw.singleFile)
            }
        }

        configureTask(FREEZE_DELETE) {
            delete netKernel.freezeDirectory
        }

        // TODO - Figure out what this task is doing...
        //configureTask(THAW_DELETE_INSTALL) {
        //    delete project.netkernel.thawInstallationDirectory
        //}

        configureTask(THAW_EXPAND) {
            from project.zipTree(netKernel.frozenArchiveFile)
            into netKernel.thawInstallationDirectory
            include '**/*'
        }

        configureTask(THAW_CONFIGURE) {
            thawDirInner netKernel.thawInstallationDirectory
        }


        configureTask(DOWNLOAD_SE) {
            downloadConfig = netKernel.download.se
            release = new Release(Edition.STANDARD)
            destinationFile = new File(fsHelper.fileInGradleHome('netkernel/download'), release.jarFileName)
        }

        configureTask(DOWNLOAD_EE) {
            downloadConfig = netKernel.download.ee
            release = new Release(Edition.ENTERPRISE)
            destinationFile = new File(fsHelper.fileInGradleHome('netkernel/download'), release.jarFileName)
        }

        configureTask(MODULE) {
            into "${project.buildDir}/${netKernel.module.name}"
            from project.sourceSets.main.output
        }

        configureTask(MODULE_RESOURCES) {
            into "${project.buildDir}/${netKernel.module.name}"
            if (netKernel.sourceStructure == NetKernelExtension.SourceStructure.GRADLE) {
                from "${project.projectDir}/src/module"
                from "${project.projectDir}/src/main/resources"
            } else {
                from "${project.projectDir}/src"
            }
        }

        // TODO: Rethink for multi modules
        configureTask(JAR) {
            from project.fileTree(dir: "${project.buildDir}/${netKernel.module.name}")
            duplicatesStrategy 'exclude'
        }

        configureTask(UPDATE_MODULE_XML_VERSION) {
            sourceModuleXml = netKernel.module.moduleFile
            outputModuleXml = project.file("${project.buildDir}/${netKernel.module.name}/module.xml")
        }

    }

    /**
     * Creates task dependencies.
     */
    void createTaskDependencies() {

        project.tasks[FREEZE_DELETE].dependsOn FREEZE_JAR
        project.tasks[FREEZE_JAR].dependsOn FREEZE_TIDY
        project.tasks[FREEZE_TIDY].dependsOn COPY_BEFORE_FREEZE
        project.tasks[JAR].dependsOn MODULE_RESOURCES
        project.tasks[JAR].dependsOn UPDATE_MODULE_XML_VERSION
        project.tasks[MODULE].dependsOn COMPILE_GROOVY
        project.tasks[MODULE_RESOURCES].dependsOn MODULE
        project.tasks[THAW_CONFIGURE].dependsOn THAW_EXPAND
        project.tasks[THAW_EXPAND].dependsOn THAW_DELETE_INSTALL
        project.tasks[UPDATE_MODULE_XML_VERSION].dependsOn MODULE_RESOURCES

    }

    /**
     * Final configuration for the project happens here.  Additionally, tasks are created for starting
     * and stopping the specified instances in the build file.
     */
    void afterEvaluate() {
        project.afterEvaluate {
            createNetKernelInstanceTasks()
        }
    }

//    def buildJarInstallerExecutionConfig(def project, def type) {
//        def config = new ExecutionConfig()
//
//        project.configure(config, {
//            name = "${type}Jar"
//            relType = type
//            release = ReleaseType.CURRENT_MAJOR_RELEASE
//            directory = fsHelper.fileInGradleHome("netkernel/install/${type}-${release}")
//            installJar = "1060-NetKernel-${type}-${release}.jar"
//            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
//        })
//
//        config
//    }
//
//    def buildInstalledExecutionConfig(def project, def type) {
//        def config = new ExecutionConfig()
//
//        project.configure(config, {
//            name = "${type}"
//            relType = type
//            release = ReleaseType.CURRENT_MAJOR_RELEASE
//            directory = fsHelper.fileInGradleHome("netkernel/install/${type}-${release}")
//            supportsDaemonModules = true
//        })
//
//        config
//    }

//    def gatherExecutionConfigs(def project, def envs) {
//        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKSE))
//        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKEE))
//        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKSE))
//        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKEE))
//    }

    /**
     * Creates instance tasks for each NetKernelInstance
     */
    void createNetKernelInstanceTasks() {
        netKernel.instances.each { NetKernelInstance instance ->
            createNetKernelInstanceTasks(instance)
        }
    }

    /**
     * Creates start/stop tasks and install task for the instance of NK.
     *
     * @param instance instance of NetKernel
     */
    void createNetKernelInstanceTasks(NetKernelInstance instance) {

        String startTaskName = "start${instance.name}"
        String stopTaskName = "stop${instance.name}"
        String installTaskName = "install${instance.name}"
        String deployTaskName = "deployTo${instance.name}"
        String undeployTaskName = "undeployFrom${instance.name}"

        createTask(startTaskName, StartNetKernelTask, "Starts NetKernel instance (${instance})")
        createTask(stopTaskName, StopNetKernelTask, "Stops NetKernel instance (${instance})")
        createTask(deployTaskName, DeployToNetKernelTask, "Deploys module(s) to instance (${instance})")
        createTask(undeployTaskName, UndeployFromNetKernelTask, "Undeploys module(s) from instance (${instance})")

        configureTask(startTaskName) {
            netKernelInstance = instance
        }

        configureTask(stopTaskName) {
            netKernelInstance = instance
        }

        configureTask(deployTaskName) {
            moduleArchiveFile = project.tasks.getByName('jar').archivePath
            netKernelInstance = instance
        }

        configureTask(undeployTaskName) {
            moduleArchiveFile = project.tasks.getByName('jar').archivePath
            netKernelInstance = instance
        }

//        // TODO - I don't think this works as expected.  Should be a clean per instance perhaps?
//        def applyCleanAllTask(def project, ExecutionConfig config) {
//            project.task("cleanAll${config.name}", type: CleanAllTask)
//                {
//                    executionConfig = config
//                }
//        }

//        if (instance.canInstall()) {
        createTask(installTaskName, InstallNetKernelTask, "Installs NetKernel instance (${instance})").setGroup(null)
        configureTask(installTaskName) {
            netKernelInstance = instance
            jarFileLocation = instance.jarFileLocation
            installDirectory = instance.location
        }

//        project.tasks[installTaskName].dependsOn "downloadNK${instance.release.edition}"
//        project.tasks[startTaskName].dependsOn installTaskName
        // TODO - Figure out task dependencies for instance tasks
//            project.tasks[installTaskName]
//        }

//        project.tasks[startTaskName].dependsOn


    }

//    def installExecutionConfigTasks(def project, ExecutionConfig config) {
//        def startNKJarName = "start${config.name}"
//        def stopNKJarName = "stop${config.name}"
//        def installNKJarName = "install${config.name}"
//
//        switch (config.mode) {
//            case ExecutionConfig.Mode.NETKERNEL_INSTALL:
//
//                project.task(startNKJarName, type: StartNetKernelTask) {
//                    configName = config.name
//                }
//
//                createTask(stopNKJarName, StopNetKernelTask, "Stops NetKernel for ${config.name} configuration.")
//                configureTask(stopNKJarName) {
//                    configName = config.name
//                }
//
//                project.task(installNKJarName, type: InstallNetKernelTask) {
//                    configName = config.name
//                }
//
//                project.tasks."${startNKJarName}".dependsOn "downloadNK${config.relType}"
//                project.tasks."${installNKJarName}".dependsOn startNKJarName
//
//                break;
//            case ExecutionConfig.Mode.NETKERNEL_FULL:
//                def startNKName = "start${config.name}"
//                String stopNKName = "stop${config.name}"
//
//                project.task(startNKName, type: StartNetKernelTask) {
//                    configName = config.name
//                }
//
//                createTask(stopNKName, StopNetKernelTask, "Stops NetKernel for ${config.name} configuration.")
//                configureTask(stopNKJarName) {
//                    configName = config.name
//                }
//
//                if (config.supportsDaemonModules) {
//                    def initDaemonDirName = "initDaemonDir${config.name}"
//                    def deployDaemonModuleName = "deployDaemonModule${config.name}"
//                    def undeployDaemonModuleName = "undeployDaemonModule${config.name}"
//
//                    project.task(initDaemonDirName, type: InitializeDaemonDirTask) {
//                        configName = config.name
//                    }
//
//                    //project.tasks."${initDaemonDirName}".dependsOn installNKJarName
//
//                    project.task(deployDaemonModuleName, type: DeployDaemonModuleTask) {
//                        configName = config.name
//                    }
//
//                    project.task(undeployDaemonModuleName, type: Delete) {
//                        delete "${config.directory}/etc/modules.d/${project.name}.xml"
//                    }
//                }
//
//                break;
//        }
//    }

    /**
     * Creates enumeration of possible NetKernel instances. This is done by looping through each edition and
     * constructing a NetKernelInstance for each one.
     *
     * @return internal gradle collection containing NetKernel instances
     */
    NamedDomainObjectContainer<NetKernelInstance> createNetKernelInstances() {
        NamedDomainObjectContainer<NetKernelInstance> instances = project.container(NetKernelInstance)

        Edition.values().each { Edition edition ->

            File location = fsHelper.fileInGradleHome("netkernel/install/${edition}-${Release.CURRENT_MAJOR_RELEASE}")
            File jarFileLocation = fsHelper.fileInGradleHome("netkernel/download/1060-NetKernel-${edition}-${Release.CURRENT_MAJOR_RELEASE}.jar")

            instances.add createNetKernelInstance(edition, location, jarFileLocation)
        }

        return instances
    }

    /**
     * Creates a NetKernelInstance reference
     *
     * @param edition Edition of NetKernel (SE or EE)
     * @param location location of netkernel instance (directory or jar file location)
     * @param jarFileLocation location of NetKernel distribution
     *
     * @return initialized NetKernelInstance
     */
    NetKernelInstance createNetKernelInstance(Edition edition, File location, File jarFileLocation) {
        // TODO - Reevaluate how the name is constructed
        String name = "${edition}"

        NetKernelInstance instance = new NetKernelInstance(
            name: name,
            release: new Release(edition),
            url: new URL('http://localhost'),
            backendPort: 1060,
            frontendPort: 8080,
            location: location,
            jarFileLocation: jarFileLocation
        )

        return instance
    }

    /**
     * Private helper method to make task creation more terse.
     *
     * @param name name of task
     * @param type class defining type of task
     * @param description description of what task does
     */
    private Task createTask(String name, Class type, String description) {
        project.tasks.create(
            name: name,
            group: "NetKernel",
            type: type,
            description: description
        )
    }

    /**
     * Private helper method to make configuration of tasks more terse.
     *
     * @param name name of task
     * @param closure closure used to configure task
     */
    private void configureTask(String name, Closure closure) {
        project.tasks[name].configure(closure)
    }

}
