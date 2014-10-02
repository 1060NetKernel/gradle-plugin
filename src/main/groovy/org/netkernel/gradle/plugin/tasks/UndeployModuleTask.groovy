package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.NetKernelInstance

/**
 * Undeploys a module from a NetKernel instance.  This is done by removing the xml file
 * from the modules.d folder on the instance.
 */
class UndeployModuleTask extends DefaultTask {

    File moduleArchiveFile

    NetKernelInstance nkinstance

    UndeployModuleTask()
    {   outputs.upToDateWhen { false }  //Force expired always
    }

    @TaskAction
    void undeploy() {
        nkinstance.undeploy(moduleArchiveFile)
    }

}
