package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.util.NetKernelHelper

/**
 *  Starts an instance of NetKernel.
 */
@Slf4j
class StartNetKernelTask extends DefaultTask {
    def NetKernelHelper nkHelper = new NetKernelHelper()

    @Input
    String configName

    @TaskAction
    def start() {
        log.info "Starting NetKernel in ${project.netkernel.envs[configName].directory}"
        nkHelper.startNetKernel(project.netkernel.envs[configName])

        log.info "Waiting for NetKernel to start..."
        while (!nkHelper.isNetKernelRunning()) {
            log.info "."
            sleep(500)
        }
    }
}
