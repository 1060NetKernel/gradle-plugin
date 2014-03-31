package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.netkernel.gradle.plugin.tasks.ListTemplateLibrariesTask
import org.netkernel.gradle.plugin.tasks.CreateNetKernelModuleFromTemplate

/**
 * Created by Randolph Kahle on 3/27/14.
 */
class NetKernelTemplatePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.configurations.create("templates")
        project.tasks.create(name: 'listTemplateLibraries', group: 'Module Creation', type: ListTemplateLibrariesTask, description: "List available templates libraries.")
        project.tasks.create(name: 'createNetKernelModuleFromTemplate', group: 'Module Creation', type: CreateNetKernelModuleFromTemplate, description: "Create a NetKernel module from a template.")
    }


}
