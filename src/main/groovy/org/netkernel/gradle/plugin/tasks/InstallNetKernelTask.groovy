package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Installs NetKernel into directory specified in the build file.
 */
class InstallNetKernelTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    def installNK() {
        netKernelInstance.install()
    }
}
