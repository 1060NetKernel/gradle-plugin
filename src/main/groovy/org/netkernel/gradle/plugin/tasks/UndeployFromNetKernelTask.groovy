package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

class UndeployFromNetKernelTask extends DefaultTask {

    @Input
    File moduleArchiveFile

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    void undeploy() {
        netKernelInstance.undeploy(moduleArchiveFile)
    }

}
