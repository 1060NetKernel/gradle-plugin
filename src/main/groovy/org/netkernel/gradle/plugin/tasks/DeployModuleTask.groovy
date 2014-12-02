package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to deploy a single built module to an instance
 */

class DeployModuleTask extends DefaultTask {
    // Static Defaults

    //Variable parameters

    //Helpers
    def propertyHelper = new PropertyHelper()

    NetKernelInstance nkinstance

    File moduleArchiveFile

    DeployModuleTask()
    {   outputs.upToDateWhen { false }      //Force expire
    }

    @org.gradle.api.tasks.TaskAction
    def deployModule()
    {
        nkinstance.deploy(moduleArchiveFile)
    }

}
