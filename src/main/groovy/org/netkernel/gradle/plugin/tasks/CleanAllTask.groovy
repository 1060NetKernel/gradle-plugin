package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.util.FileSystemHelper

@Slf4j
class CleanAllTask extends DefaultTask {
    FileSystemHelper fsHelper = new FileSystemHelper()

    @TaskAction
    def cleanAll() {
        File netKernelDirectory = fsHelper.dirInGradleHomeDirectory("netkernel")
        log.debug "Deleting ${netKernelDirectory}"
        netKernelDirectory.deleteDir()
    }
}
