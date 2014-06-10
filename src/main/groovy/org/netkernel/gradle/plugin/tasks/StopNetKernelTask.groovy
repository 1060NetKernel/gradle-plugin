package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Stops an instance of NetKernel.
 */
@Slf4j
class StopNetKernelTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    void stopNetKernel() {
        log.info "Stopping NetKernel at ${netKernelInstance}"
        netKernelInstance.stop()
    }

}
