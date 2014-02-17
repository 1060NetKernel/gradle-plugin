package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.ExecutionConfig
import org.netkernel.gradle.util.NetKernelHelper

/**
 *  Starts an instance of NetKernel.
 */
class StartNetKernelTask extends DefaultTask {
    def NetKernelHelper nkHelper = new NetKernelHelper()
    def configName
    def ExecutionConfig executionConfig

    @TaskAction
    def start() {
        println "Starting NetKernel"
        nkHelper.startNetKernel(project.netkernel.envs[configName])
        println "Waiting for NetKernel to start..."

        while(!nkHelper.isNetKernelRunning()) {
            print "."
            sleep(500)
        }
    }
}
