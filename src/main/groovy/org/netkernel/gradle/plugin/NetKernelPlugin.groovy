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
import org.netkernel.gradle.plugin.tasks.ConfigureApposite
import org.netkernel.gradle.plugin.tasks.CreateAppositePackage
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

    public static final String NETKERNELSRC = "NETKERNELSRC"
    public static final String GRADLESRC = "GRADLESRC"

    def fsHelper = new FileSystemHelper()
    ModuleHelper moduleHelper = null;

    Project project
    NetKernelExtension netKernel

    void apply(Project project) {
        this.project = project

        project.apply plugin: 'groovy'
        project.apply plugin: 'maven'

        // Move this guy out
        def configName = "SE"

        ['freeze', 'thaw', 'provided'].each { name ->
            project.configurations.create(name)
        }
        project.configurations.compile.extendsFrom(project.configurations.provided)

        def envs = project.container(ExecutionConfig)
        gatherExecutionConfigs(project, envs)

        netKernel = project.extensions.create("netkernel", NetKernelExtension, project, envs, configName)

        //FREEZE SETUP

        //BRIAN how do we parameterise this well?
//        def config = project.netkernel.envs[configName]
//        def installationDir = config.directory
//        def dest = fsHelper.dirInGradleHomeDirectory("netkernel")
//        def freezeDir = fsHelper.dirInGradleHomeDirectory("netkernel/freeze")
//        def thawDir = fsHelper.dirInGradleHomeDirectory("netkernel/thaw")

        createTasks()

//        project.configure([project.tasks.installFreeze, project.tasks.uploadFreeze]) {
//            repositories.withType(org.gradle.api.artifacts.maven.MavenResolver) {
//                pom.groupId = "org.netkernel"
//                pom.version = "1.1.1"
//                pom.artifactId = "frozenInstance"
//                pom.scopeMappings.mappings.clear()
//            }
//        }

        //BRIAN how do we maven publish?
        /*
        project.task('freezePublish', type: org.gradle.api.publish.maven.MavenPublication ){
        }
        project.tasks.freezePublish.dependsOn "freezeJar"
        */

        // THAW SETUP

        //BRIAN how to we retrieve from maven?

//        def thawInstallationDir = installationDir.absolutePath + "2"
//        project.task('thawDeleteInstall', type: Delete) {
//            delete thawInstallationDir
//        }

//        def frozenJar = new File(dest, "frozen.zip")
//        project.task('thawExpand', type: Copy) {
//            from(project.zipTree(frozenJar))
//            into thawInstallationDir
//            include "**/*"
//        }


//        project.task('thawConfigure', type: ThawConfigureTask) {
//            thawDirInner = thawInstallationDir
//        }


        project.task('configureApposite', type: ConfigureApposite) {

        }


        project.task('createAppositePackage', type: CreateAppositePackage) {

        }

        project.task('downloadNKSE', type: DownloadNetKernelTask) {
            downloadConfig = netKernel.download.se
        }

        project.task('downloadNKEE', type: DownloadNetKernelTask) {
            downloadConfig = netKernel.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }

        // Module-specific tasks

        def sourceStructure
        def projectDir = project.projectDir
        if (new File(projectDir, "src/module.xml").exists()) {
            sourceStructure = NETKERNELSRC
        } else {
            sourceStructure = GRADLESRC
        }
        //println("sourceStructure="+sourceStructure)

        project.ext.nkModuleIdentity = null

        switch (sourceStructure) {
            case NETKERNELSRC:
                def baseDir = new File(projectDir, 'src/')
                //Configure the javaCompiler
                def fileTree = project.fileTree(dir: baseDir, includes: ['**/*.java'])
                //fileTree.visit { f ->  println f }
                project.tasks.compileJava.configure {
                    source = fileTree
                }
                //Configure the groovyCompiler
                fileTree = project.fileTree(dir: new File(projectDir, 'src/'), includes: ['**/*.groovy'])
                project.tasks.compileGroovy.configure {
                    source = fileTree
                }
                moduleHelper = new ModuleHelper("${project.projectDir}/src/module.xml")

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
            case GRADLESRC:
                if (project.file("${project.projectDir}/src/module/module.xml").exists()) {
                    moduleHelper = new ModuleHelper("${project.projectDir}/src/module/module.xml")
                }
                if (project.file("${project.projectDir}/src/main/resources/module.xml").exists()) {
                    moduleHelper = new ModuleHelper("${project.projectDir}/src/main/resources/module.xml")
                }
                break;
        }

        if (!moduleHelper) {
            throw new InvalidUserDataException("Could not find module.xml in the project.")
        }

        // If the project has a version specified, override the value in the module.xml
        if (project.version == 'unspecified') {
            project.version = moduleHelper.version
        } else {
            moduleHelper.version = project.version
        }

        //Set up module identity and maven artifact
        project.ext.nkModuleIdentity = moduleHelper.name

        //Set Maven Artifact name and version
        //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
        project.archivesBaseName = moduleHelper.URIDotted

        //println "MODULE TARGET ${project.ext.nkModuleIdentity}"
        //println("Finished configuring srcStructure")

        project.task('module', type: Copy) {
            into "${project.buildDir}/${project.ext.nkModuleIdentity}"
            from project.sourceSets.main.output
        }


        project.task('moduleResources', type: ModuleResourcesTask) {
            into "${project.buildDir}/${project.ext.nkModuleIdentity}"
            if (sourceStructure.equals(GRADLESRC)) {
                from "${project.projectDir}/src/module"
                from "${project.projectDir}/src/main/resources"
            }

            if (sourceStructure.equals(NETKERNELSRC)) {
                from "${project.projectDir}/src"
            }
        }

        // TODO: Rethink this for multi modules
        project.tasks.jar.configure {
            from project.fileTree(dir: "${project.buildDir}/${project.ext.nkModuleIdentity}")
            duplicatesStrategy 'exclude'
        }

        if (moduleHelper.versionOverridden) {
            project.task('updateModuleXmlVersion', type: UpdateModuleXmlVersionTask) {
                sourceModuleXml = project.file(moduleHelper.moduleFilePath)
                outputModuleXml = project.file("${project.buildDir}/${project.ext.nkModuleIdentity}/module.xml")
            }
            project.tasks.updateModuleXmlVersion.dependsOn 'moduleResources'
            project.tasks.jar.dependsOn 'updateModuleXmlVersion'
        }



        project.afterEvaluate {
            project.netkernel.envs.each { c ->
                installExecutionConfigTasks(project, c)
                applyCleanAllTask(project, c)
            }
        }

        configureTasks()
        setupTaskDependencies()
        configureProject()
    }


    void createTasks() {
        final String netKernelGroupName = "NetKernel"

        project.tasks.create(
            name: COPY_BEFORE_FREEZE,
            group: netKernelGroupName,
            type: Copy,
            description: "Copies NetKernel installation into freeze directory"
        )

        project.tasks.create(
            name: FREEZE_TIDY,
            group: netKernelGroupName,
            type: FreezeTidyTask,
            description: 'Cleans up copied NetKernel instance during freeze'
        )

        project.tasks.create(
            name: FREEZE_JAR,
            group: netKernelGroupName,
            type: Jar,
            description: 'Creates zip file of frozen NetKernel instance'
        )

        project.tasks.create(
            name: INSTALL_FREEZE,
            group: netKernelGroupName,
            type: Upload,
            description: "Installs frozen NetKernel instance into maven repository"
        )

        project.tasks.create(
            name: THAW,
            group: netKernelGroupName,
            type: Copy,
            description: "Copies frozen NetKernel instance into thaw directory"
        )
        project.tasks[THAW].setEnabled(project.configurations.thaw.files.size() == 1)

        project.tasks.create(
            name: FREEZE_DELETE,
            group: netKernelGroupName,
            type: Delete,
            description: "Deletes frozen NetKernel instance"
        )

        // TODO - Figure out what this task is doing...
        project.tasks.create(
            name: THAW_DELETE_INSTALL,
            group: netKernelGroupName,
            type: Delete,
            description: "Deletes thawed installation directory"
        )

        project.tasks.create(
            name: THAW_EXPAND,
            group: netKernelGroupName,
            type: Copy,
            description: "Expands thawed NetKernel instance into thaw installation directory"
        )

        project.tasks.create(
            name: THAW_CONFIGURE,
            group: netKernelGroupName,
            type: ThawConfigureTask,
            description: "Thaws configuration for NetKernel instance"
        )

        /*        project.task('freezePublish', type: org.gradle.api.publish.maven.tasks.PublishToMavenLocal) {
            publication {
                from freezeDir
                artifact 'frozen.zip'
            }
        } */

    }

    void configureTasks() {

        project.tasks[COPY_BEFORE_FREEZE].configure {
            from netKernel.installationDirectory
            into netKernel.freezeDirectory
            include "**/*"
        }
        project.tasks[FREEZE_TIDY].configure {
            freezeDirInner netKernel.freezeDirectory
            installDirInner netKernel.installationDirectory
        }
        project.tasks[FREEZE_JAR].configure {
            from netKernel.freezeDirectory
            destinationDir = netKernel.destinationDirectory
            archiveName = 'frozen.zip'
        }
        project.tasks[INSTALL_FREEZE].configure {
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
            project.tasks[THAW].configure {
                into netKernel.thawDirectory
                from project.zipTree(project.configurations.thaw.singleFile)
            }
        }

        project.tasks[FREEZE_DELETE].configure {
            delete netKernel.freezeDirectory
        }

        // TODO - Figure out what this task is doing...
        //project.tasks[THAW_DELETE_INSTALL].configure {
        //    delete project.netkernel.thawInstallationDirectory
        //}

        project.tasks[THAW_EXPAND].configure {
            from project.zipTree(netKernel.frozenArchiveFile)
            into netKernel.thawInstallationDirectory
            include '**/*'
        }

        project.tasks[THAW_CONFIGURE].configure {
            thawDirInner netKernel.thawInstallationDirectory
        }
    }

    void configureProject() {
//        project.artifacts {
//            freeze project.tasks[FREEZE_JAR].outputs
//        }
    }

    void setupTaskDependencies() {
        project.tasks[FREEZE_TIDY].dependsOn COPY_BEFORE_FREEZE
        project.tasks[FREEZE_JAR].dependsOn FREEZE_TIDY
        project.tasks[FREEZE_DELETE].dependsOn FREEZE_JAR
        project.tasks[THAW_EXPAND].dependsOn THAW_DELETE_INSTALL
        project.tasks[THAW_CONFIGURE].dependsOn THAW_EXPAND
        project.tasks[MODULE].dependsOn COMPILE_GROOVY
        project.tasks[MODULE_RESOURCES].dependsOn MODULE
        project.tasks[JAR].dependsOn MODULE_RESOURCES
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



}
