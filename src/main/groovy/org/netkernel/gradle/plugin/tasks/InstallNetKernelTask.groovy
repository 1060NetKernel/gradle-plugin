package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Installs NetKernel into location of NetKernelInstance.
 */
class InstallNetKernelTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    def installNetKernel() {
        // Install NetKernel instance to installDirectory
        netKernelInstance.install()

        // Create modules.d folder and add properties to etc/kernel.properties
        netKernelInstance.initializeModulesDir()
    }

}
