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
        println "Waiting for NetKernel to start..."
        def loops=0
        while (!netKernelInstance.isRunning()) {
            print "."
            sleep(500)
            loops++
            if(loops==120)
            {   throw new Exception("!!!!!!!!!!! Unable to start ${netKernelInstance}")
            }
            // TODO - Think about timeout here
        }
    }
}
