package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Installs NetKernel into location of NetKernelInstance.
 */
class InstallNetKernelTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @OutputDirectory
    File installDirectory

    @TaskAction
    def installNK() {
        netKernelInstance.install()
    }

    /**
     * Used to leverage gradle's incremental build system.  If the installDirectory
     * hasn't changed, then this task will not fire.
     *
     * @param netKernelInstance NetKernel instance
     */
    void setNetKernelInstance(NetKernelInstance netKernelInstance) {
        this.netKernelInstance = netKernelInstance
        this.installDirectory = netKernelInstance.location
    }
}
