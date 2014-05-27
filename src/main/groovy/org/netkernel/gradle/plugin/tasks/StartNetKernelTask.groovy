package org.netkernel.gradle.plugin.tasks
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.util.NetKernelHelper
/**
 *  Starts an instance of NetKernel.
 */
class StartNetKernelTask extends DefaultTask {
    def NetKernelHelper nkHelper = new NetKernelHelper()
    def configName

    @TaskAction
    def start() {
        println "Starting NetKernel"
        nkHelper.startNetKernel(project.netkernel.envs[configName])

        println project.netkernel.envs[configName].directory
        println "Waiting for NetKernel to start..."

        while(!nkHelper.isNetKernelRunning()) {
            print "."
            sleep(500)
        }
    }
}
