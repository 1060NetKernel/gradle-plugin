package org.netkernel.gradle.plugin.tasks

import org.gradle.api.tasks.Copy
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to deploy a single built module to an instance
 */

class DeployLicenseTask extends Copy {
    // Static Defaults

    //Variable parameters

    //Helpers

    DeployLicenseTask()
    {   outputs.upToDateWhen { false }      //Force expire
    }

}
