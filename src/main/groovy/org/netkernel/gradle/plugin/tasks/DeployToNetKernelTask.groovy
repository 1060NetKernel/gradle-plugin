package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Created by randolph.kahle on 4/1/14.
 */
class DeployToNetKernelTask extends DefaultTask {

    @Input
    File moduleArchiveFile

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    void deployToNetKernel() {
        netKernelInstance.deploy(moduleArchiveFile)
    }

}
