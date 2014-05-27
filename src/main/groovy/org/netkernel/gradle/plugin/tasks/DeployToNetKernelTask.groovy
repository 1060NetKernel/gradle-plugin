package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.plugin.util.FileSystemHelper
import org.netkernel.gradle.plugin.util.NetKernelHelper

/**
 * Created by randolph.kahle on 4/1/14.
 */
class DeployToNetKernelTask extends DefaultTask {

    def FileSystemHelper fileSystemHelper = new FileSystemHelper()
    def NetKernelHelper netKernelHelper = new NetKernelHelper()

    @org.gradle.api.tasks.TaskAction
    void deployToNetKernel() {

        println 'Hi! I am happy to deploy your modules to NetKernel'

        project.configurations.getByName('netkernelDeploy').dependencies.each {
            println ''
            println '---- Modules To Deploy ----'
            project.configurations.getByName('netkernelDeploy').fileCollection(it).each {
                File libraryJarFile = it
                println libraryJarFile.name
            }
        }

        // Determine where NetKernel is installed.
        // We can do this by asking the running NetKernel or we use a set parameter

        if (!netKernelHelper.isNetKernelRunning()) {
            println 'NetKernel is not running.'
        } else {
            println 'NetKernel is running we can continue...'
            netKernelHelper.setNetKernelModulesExtensionDirectory()
            def installFilePath = netKernelHelper.whereIsNetKernelInstalled()
            def modulesDFilePath = netKernelHelper.whereIsModuleExtensionDirectory()
            println installFilePath + modulesDFilePath

            

        }


    }

}
