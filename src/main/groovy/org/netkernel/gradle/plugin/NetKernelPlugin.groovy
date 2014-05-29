package org.netkernel.gradle.plugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.netkernel.gradle.plugin.nk.ExecutionConfig
import org.netkernel.gradle.plugin.nk.ReleaseType
import org.netkernel.gradle.plugin.tasks.CleanAllTask
import org.netkernel.gradle.plugin.tasks.ConfigureAppositeTask
import org.netkernel.gradle.plugin.tasks.CreateAppositePackageTask
import org.netkernel.gradle.plugin.tasks.DeployDaemonModuleTask
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask
import org.netkernel.gradle.plugin.tasks.FreezeTidyTask
import org.netkernel.gradle.plugin.tasks.InitializeDaemonDirTask
import org.netkernel.gradle.plugin.tasks.InstallNetKernelTask
import org.netkernel.gradle.plugin.tasks.ModuleResourcesTask
import org.netkernel.gradle.plugin.tasks.StartNetKernelTask
import org.netkernel.gradle.plugin.tasks.ThawConfigureTask
import org.netkernel.gradle.plugin.tasks.UpdateModuleXmlVersionTask
import org.netkernel.gradle.plugin.util.FileSystemHelper
import org.netkernel.gradle.plugin.util.ModuleHelper

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

        // TODO - Do we always want to apply groovy & maven plugins?  What about other languages like kotlin, scala, etc.?
        project.apply plugin: 'groovy'
        project.apply plugin: 'maven'

        configureProject()
        createTasks()
        configureTasks()
        createTaskDependencies()
        postProjectConfiguration()
    }

    /**
     * Initial configuration for the plugin.  This is where the backing model is initialized
     * and configurations are added.  Also determined is the source structure of the project,
     * be it NetKernel or gradle.
     */
    void configureProject() {
        // TODO -  Move this guy out
        def configName = "SE"

        ['freeze', 'thaw', 'provided'].each { name ->
            project.configurations.create(name)
        }
        project.configurations.compile.extendsFrom(project.configurations.provided)

        def envs = project.container(ExecutionConfig)
        gatherExecutionConfigs(project, envs)
        netKernel = project.extensions.create("netkernel", NetKernelExtension, project, envs, configName)

        File projectDirectory = project.projectDir
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
                netKernel.moduleHelper = new ModuleHelper("${project.projectDir}/src/module.xml")

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
                if (project.file("${project.projectDir}/src/module/module.xml").exists()) {
                    netKernel.moduleHelper = new ModuleHelper("${project.projectDir}/src/module/module.xml")
                }
                if (project.file("${project.projectDir}/src/main/resources/module.xml").exists()) {
                    netKernel.moduleHelper = new ModuleHelper("${project.projectDir}/src/main/resources/module.xml")
                }
                break;
        }

        if (!netKernel.moduleHelper) {
            throw new InvalidUserDataException("Could not find module.xml in the project.")
        }

        // If the project has a version specified, override the value in the module.xml
        if (project.version == 'unspecified') {
            project.version = netKernel.moduleHelper.version
        } else {
            netKernel.moduleHelper.version = project.version
        }


        //Set Maven Artifact name and version
        //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
        project.archivesBaseName = netKernel.moduleHelper.URIDotted
    }

    /**
     * Creates all of the tasks for this plugin.  Some tasks may be disabled depending on
     * what is provided by the build.gradle file.
     */
    void createTasks() {

        createTask(COPY_BEFORE_FREEZE, Copy, 'Copies NetKernel installation into freeze directory')

        createTask(FREEZE_TIDY, FreezeTidyTask, 'Cleans up copied NetKernel instance during freeze')

        createTask(FREEZE_JAR, Jar, 'Creates zip file of frozen NetKernel instance')

        createTask(INSTALL_FREEZE, Upload, "Installs frozen NetKernel instance into maven repository")

        createTask(THAW, Copy, "Copies frozen NetKernel instance into thaw directory")
        project.tasks[THAW].setEnabled(project.configurations.thaw.files.size() == 1)

        createTask(FREEZE_DELETE, Delete, "Deletes frozen NetKernel instance")

        // TODO - Figure out what this task is doing...
        createTask(THAW_DELETE_INSTALL, Delete, "Deletes thawed installation directory")

        createTask(THAW_EXPAND, Copy, "Expands thawed NetKernel instance into thaw installation directory")

        createTask(THAW_CONFIGURE, ThawConfigureTask, "Thaws configuration for NetKernel instance")

        /*        project.task('freezePublish', type: org.gradle.api.publish.maven.tasks.PublishToMavenLocal) {
            publication {
                from freezeDir
                artifact 'frozen.zip'
            }
        } */

        createTask(CONFIGURE_APPOSITE, ConfigureAppositeTask, 'Configures apposite repository')

        createTask(CREATE_APPOSITE_PACKAGE, CreateAppositePackageTask, 'Creates apposite package')

        createTask(DOWNLOAD_NKSE, DownloadNetKernelTask, "Downloads NetKernel Standard Edition")

        createTask(DOWNLOAD_NKEE, DownloadNetKernelTask, "Downloads NetKernel Enterprise Edition")

        createTask(MODULE, Copy, "Copies built classes to build folder")

        createTask(MODULE_RESOURCES, ModuleResourcesTask, 'Copies module resources (module.xml, etc.) to build folder')

        createTask(UPDATE_MODULE_XML_VERSION, UpdateModuleXmlVersionTask, 'Updates version in module xml to match project version')
        project.tasks[UPDATE_MODULE_XML_VERSION].setEnabled(netKernel.moduleHelper.versionOverridden)
    }

    /**
     * Any custom configuration for the tasks is done here.  Specific data needed by the tasks
     * is provided by the backing model class ({@see NetKernelExtension})
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

        configureTask(DOWNLOAD_NKSE) {
            downloadConfig = netKernel.download.se
        }

        configureTask(DOWNLOAD_NKEE) {
            downloadConfig = netKernel.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }

        configureTask(MODULE) {
            into "${project.buildDir}/${netKernel.moduleHelper.name}"
            from project.sourceSets.main.output
        }

        configureTask(MODULE_RESOURCES) {
            into "${project.buildDir}/${netKernel.moduleHelper.name}"
            if (netKernel.sourceStructure == NetKernelExtension.SourceStructure.GRADLE) {
                from "${project.projectDir}/src/module"
                from "${project.projectDir}/src/main/resources"
            } else {
                from "${project.projectDir}/src"
            }
        }

        // TODO: Rethink for multi modules
        configureTask(JAR) {
            from project.fileTree(dir: "${project.buildDir}/${netKernel.moduleHelper.name}")
            duplicatesStrategy 'exclude'
        }

        configureTask(UPDATE_MODULE_XML_VERSION) {
            sourceModuleXml = project.file(netKernel.moduleHelper.moduleFilePath)
            outputModuleXml = project.file("${project.buildDir}/${netKernel.moduleHelper.name}/module.xml")
        }

    }

    /**
     * Creates task dependencies.
     */
    void createTaskDependencies() {
        project.tasks[FREEZE_TIDY].dependsOn COPY_BEFORE_FREEZE
        project.tasks[FREEZE_JAR].dependsOn FREEZE_TIDY
        project.tasks[FREEZE_DELETE].dependsOn FREEZE_JAR
        project.tasks[THAW_EXPAND].dependsOn THAW_DELETE_INSTALL
        project.tasks[THAW_CONFIGURE].dependsOn THAW_EXPAND
        project.tasks[MODULE].dependsOn COMPILE_GROOVY
        project.tasks[MODULE_RESOURCES].dependsOn MODULE
        project.tasks[JAR].dependsOn MODULE_RESOURCES
        project.tasks[UPDATE_MODULE_XML_VERSION].dependsOn MODULE_RESOURCES
        project.tasks[JAR].dependsOn UPDATE_MODULE_XML_VERSION
    }

    /**
     * Final configuration for the project happens here.
     */
    void postProjectConfiguration() {
        project.afterEvaluate {
            netKernel.envs.each { c ->
                installExecutionConfigTasks(project, c)
                applyCleanAllTask(project, c)
            }
        }
    }


    def buildJarInstallerExecutionConfig(def project, def type) {
        def config = new ExecutionConfig()

        project.configure(config, {
            name = "${type}Jar"
            relType = type
            release = ReleaseType.CURRENT_MAJOR_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/${type}-${release}")
            installJar = "1060-NetKernel-${type}-${release}.jar"
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })

        config
    }

    def buildInstalledExecutionConfig(def project, def type) {
        def config = new ExecutionConfig()

        project.configure(config, {
            name = "${type}"
            relType = type
            release = ReleaseType.CURRENT_MAJOR_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/${type}-${release}")
            supportsDaemonModules = true
        })

        config
    }

    def gatherExecutionConfigs(def project, def envs) {
        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKSE))
        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKEE))
        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKSE))
        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKEE))
    }

    def applyCleanAllTask(def project, ExecutionConfig config) {
        project.task("cleanAll${config.name}", type: CleanAllTask)
            {
                executionConfig = config
            }
    }

    def installExecutionConfigTasks(def project, ExecutionConfig config) {
        def startNKJarName = "start${config.name}"
        def installNKJarName = "install${config.name}"

        switch (config.mode) {
            case ExecutionConfig.Mode.NETKERNEL_INSTALL:

                project.task(startNKJarName, type: StartNetKernelTask) {
                    configName = config.name
                }

                project.task(installNKJarName, type: InstallNetKernelTask) {
                    configName = config.name
                }

                project.tasks."${startNKJarName}".dependsOn "downloadNK${config.relType}"
                project.tasks."${installNKJarName}".dependsOn startNKJarName

                break;
            case ExecutionConfig.Mode.NETKERNEL_FULL:
                def startNKName = "start${config.name}"

                project.task(startNKName, type: StartNetKernelTask) {
                    configName = config.name
                }

                if (config.supportsDaemonModules) {
                    def initDaemonDirName = "initDaemonDir${config.name}"
                    def deployDaemonModuleName = "deployDaemonModule${config.name}"
                    def undeployDaemonModuleName = "undeployDaemonModule${config.name}"

                    project.task(initDaemonDirName, type: InitializeDaemonDirTask) {
                        configName = config.name
                    }

                    //project.tasks."${initDaemonDirName}".dependsOn installNKJarName

                    project.task(deployDaemonModuleName, type: DeployDaemonModuleTask) {
                        configName = config.name
                    }

                    project.task(undeployDaemonModuleName, type: Delete) {
                        delete "${config.directory}/etc/modules.d/${project.name}.xml"
                    }
                }

                break;
        }
    }

    /**
     * Private helper method to make task creation more terse.
     *
     * @param name name of task
     * @param type class defining type of task
     * @param description description of what task does
     */
    private void createTask(String name, Class type, String description) {
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
