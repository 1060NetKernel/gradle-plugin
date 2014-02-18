package org.netkernel.gradle.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.netkernel.gradle.nk.ExecutionConfig
import org.netkernel.gradle.nk.ReleaseType
import org.netkernel.gradle.plugin.tasks.*
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.ModuleHelper
/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    def fsHelper = new FileSystemHelper()
    def moduleHelper = new ModuleHelper()
    
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

    def applyCleanAllTask(def project, ExecutionConfig config)
    {		project.task("cleanAll${config.name}", type: CleanAllTask)
    		{	executionConfig = config
    		}
    }
    
    def installExecutionConfigTasks(def project, ExecutionConfig config) {
        def startNKJarName = "start${config.name}"
        def installNKJarName = "install${config.name}"

        switch(config.mode) {
            case ExecutionConfig.Mode.NETKERNEL_INSTALL :

                project.task(startNKJarName, type: StartNetKernelTask) {
                    configName = config.name
                }

                project.task(installNKJarName, type: InstallNetKernelTask) {
                    configName = config.name
                }

                project.tasks."${startNKJarName}".dependsOn "downloadNK${config.relType}"
                project.tasks."${installNKJarName}".dependsOn startNKJarName

                break;
            case ExecutionConfig.Mode.NETKERNEL_FULL :
                def startNKName = "start${config.name}"

                project.task(startNKName, type: StartNetKernelTask) {
                    configName = config.name
                }

                if(config.supportsDaemonModules) {
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

    void apply(Project project) {
        project.apply plugin: 'groovy'
        
        def envs = project.container(ExecutionConfig)
        gatherExecutionConfigs(project, envs)

        def extension = project.extensions.create("netkernel", NetKernelExtension, project, envs)

        project.task('downloadNKSE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.se
        }

        project.task('downloadNKEE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }

        // Module-specific tasks
        
        project.task('module', type: Copy) {
            into "${project.buildDir}/${project.name}"
            from project.sourceSets.main.output
        }
        
        project.tasks.module.dependsOn "compileGroovy"
        
        project.task('moduleResources', type: Copy) {
            into "${project.buildDir}/${project.name}"
            from "${project.projectDir}/src/module"
        }
        
        project.tasks.moduleResources.dependsOn "module"
        
        // TODO: Rethink this for multi modules
        
        def jarName = moduleHelper.getModuleArchiveName("${project.projectDir}/src/module/module.xml")

        project.tasks.jar.configure {
            destinationDir=project.file("${project.buildDir}/modules")
            archiveName=jarName
            from project.fileTree(dir:"${project.buildDir}/${project.name}")
            duplicatesStrategy 'exclude'
        }
        
        project.tasks.jar.dependsOn 'moduleResources'

        project.afterEvaluate {
            project.netkernel.envs.each { c ->
                installExecutionConfigTasks(project, c)
                applyCleanAllTask(project ,c)
            }
        }
        
        

        addNetKernelConfiguration(project)
    }
    
    def addNetKernelConfiguration(Project project) {
      /*  project.sourceSets {
            project.configure([main, test]) {
                project.configure([java,groovy]) {
                    srcDirs = [project.projectDir]
                }
            }
        } */

    }
}
