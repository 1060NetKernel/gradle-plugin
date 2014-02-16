package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.netkernel.gradle.plugin.tasks.DeployDaemonModuleTask
import org.netkernel.gradle.plugin.tasks.DownloadNetKernelTask
import org.netkernel.gradle.plugin.tasks.InstallNetKernelTask
import org.netkernel.gradle.plugin.tasks.InitializeDaemonDirTask
import org.netkernel.gradle.plugin.tasks.StartNetKernelTask
import org.netkernel.gradle.plugin.tasks.CleanAllTask
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.ModuleHelper

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    def fsHelper = new FileSystemHelper()
    def moduleHelper = new ModuleHelper()
    
    def CURRENT_MAJOR_NK_RELEASE = '5.2.1'

    void apply(Project project) {
        project.apply plugin: 'groovy'
        
        def envs = project.container(ExecutionConfig)

        def defaultSEJar = new ExecutionConfig()

        project.configure(defaultSEJar, {
            release = CURRENT_MAJOR_NK_RELEASE
            installJar = "1060-NetKernel-SE-${release}.jar"
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/SE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })

        def defaultEEJar = new ExecutionConfig()

        project.configure(defaultEEJar,  {
            release = CURRENT_MAJOR_NK_RELEASE
            installJar = "1060-NetKernel-EE-${release}.jar"
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/EE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })
        
        def defaultSEInstalled = new ExecutionConfig()
        
        project.configure(defaultSEInstalled, {
            release = CURRENT_MAJOR_NK_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/SE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_FULL
        })
        
        def defaultEEInstalled = new ExecutionConfig()
        
        project.configure(defaultEEInstalled, {
            release = CURRENT_MAJOR_NK_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/EE-${release}")
            mode = ExecutionConfig.Mode.NETKERNEL_FULL
        })       

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

        // Default SE Installation Behavior

        project.task('startNKSEJar', type: StartNetKernelTask) {
            executionConfig = defaultSEJar
        }
        
        project.tasks.startNKSEJar.dependsOn "downloadNKSE"
        project.task('installNKSE', type: InstallNetKernelTask) {
            executionConfig = defaultSEJar
        }
        project.tasks.installNKSE.dependsOn "startNKSEJar"

        // Default EE Installation Behavior

        project.task('startNKEEJar', type: StartNetKernelTask) {
            executionConfig = defaultEEJar
        }
        project.tasks.startNKEEJar.dependsOn "downloadNKEE"
        
        project.task('installNKEE', type: InstallNetKernelTask) {
            executionConfig = defaultEEJar
        }
        project.tasks.installNKEE.dependsOn "startNKEEJar"
        
        // Default SE Start Behavior
        
        project.task('startNKSE', type: StartNetKernelTask) {
            executionConfig = defaultSEInstalled
        }
        
        // Default EE Start Behavior
        
        project.task('startNKEE', type: StartNetKernelTask) {
            executionConfig = defaultEEInstalled
        }

        // TODO: Add the above behavior for every environment
        
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
        
        // Deployment Tasks
        
        project.task('initDaemonDirNKSE', type: InitializeDaemonDirTask) {
            executionConfig = defaultSEInstalled
        }
        
        project.tasks.initDaemonDirNKSE.dependsOn "installNKSE"
        
        project.task('initDaemonDirNKEE', type: InitializeDaemonDirTask) {
            executionConfig = defaultEEInstalled
        }
        
        project.tasks.initDaemonDirNKEE.dependsOn "installNKEE"
        
        project.task('deployDaemonModuleNKSE', type: DeployDaemonModuleTask) {
            executionConfig = defaultSEInstalled
        }
        
        project.task('deployDaemonModuleNKEE', type: DeployDaemonModuleTask) {
            executionConfig = defaultEEInstalled
        }
        
        project.task('undeployDaemonModuleNKSE', type: Delete) {
            delete "${defaultSEInstalled.directory}/etc/modules.d/${project.name}.xml"
        }
        
        project.task('undeployDaemonModuleNKEE', type: Delete) {
            delete "${defaultEEInstalled.directory}/etc/modules.d/${project.name}.xml"
        }
        
        // TODO: Rethink this for multi modules
        
        def jarName = moduleHelper.getModuleArchiveName("${project.projectDir}/src/module/module.xml")

        project.tasks.jar.configure {
            destinationDir=project.file("${project.buildDir}/modules")
            archiveName=jarName
            from project.fileTree(dir:"${project.buildDir}/${project.name}")
            duplicatesStrategy 'exclude'
        }
        
        project.tasks.jar.dependsOn 'moduleResources'
        
        // TODO: Support other executionConfigs
        
        //Housekeeping Tasks
        project.task('cleanAll', type: CleanAllTask) {
            executionConfig = defaultEEJar
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
