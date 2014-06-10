package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 *  Starts an instance of NetKernel.
 */
@Slf4j
class StartNetKernelTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    def start() {
        log.info "Starting NetKernel instance ${netKernelInstance}"
        netKernelInstance.start()

        log.info "Waiting for NetKernel to start..."
        while (!netKernelInstance.isRunning()) {
            log.info "."
            sleep(500)
            // TODO - Think about timeout here
        }
    }
}
