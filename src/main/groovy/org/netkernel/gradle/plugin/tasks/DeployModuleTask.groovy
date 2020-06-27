package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.netkernel.gradle.plugin.model.NetKernelInstance

import groovy.util.logging.Slf4j;

/*
 * A task to deploy a single built module to an instance
 */
@Slf4j
class DeployModuleTask extends DefaultTask {
    
    @Input
    NetKernelInstance netKernelInstance

    @InputFile
    File moduleArchiveFile
    
    @org.gradle.api.tasks.TaskAction
    def deployModule()
    {
        netKernelInstance.deploy(moduleArchiveFile)
    }

}
