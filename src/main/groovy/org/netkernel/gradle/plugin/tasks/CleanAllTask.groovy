package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.nk.ExecutionConfig

@Slf4j
class CleanAllTask extends DefaultTask {

    ExecutionConfig executionConfig

    @TaskAction
    def cleanAll() {
        File netKernelDirectory = executionConfig.directory
        log.info "Cleaning up ${netKernelDirectory}"
        netKernelDirectory.deleteDir()
    }
}
