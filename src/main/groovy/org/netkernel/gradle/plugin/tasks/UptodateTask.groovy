package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Is instance up to date
 */
class UptodateTask extends DefaultTask {

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    def uptodate() {
        if(netKernelInstance.isRunning()) {
            if(!netKernelInstance.isUpToDate())
            {   throw new Exception("${netKernelInstance.name} is not up to date - run appositeUpdate${netKernelInstance.name}")
            }
        }
        else throw new Exception ("${netKernelInstance.name} is not running - please start it to run xunit tests")
    }

}
