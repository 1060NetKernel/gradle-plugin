package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.nk.ExecutionConfig
import org.netkernel.gradle.plugin.util.NetKernelHelper

/**
 * Stops an instance of NetKernel
 */
@Slf4j
class StopNetKernelTask extends DefaultTask {

    NetKernelHelper netKernelHelper = new NetKernelHelper()

    @Input
    String configName

    @TaskAction
    void stopNetKernel() {
        ExecutionConfig executionConfig = project.netkernel.envs[configName]
        log.info "Stopping NetKernel at ${executionConfig.backendFulcrum}"
        netKernelHelper.stopNetKernel(executionConfig)
    }

}
