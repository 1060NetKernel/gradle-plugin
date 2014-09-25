package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by randolph.kahle on 4/1/14.
 */
class NetKernelDeployPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.configurations.create("netkernelDeploy")
        project.tasks.create(name: 'deployToNetKernel', group: 'NetKernel Management', type: DeployToNetKernelTask, description: "Deploy dependent modules to NetKernel.")
    }

}
