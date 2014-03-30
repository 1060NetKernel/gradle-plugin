package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.netkernel.gradle.plugin.tasks.ListTemplateLibrariesTask
import org.netkernel.gradle.plugin.tasks.ListTemplatesTask

/**
 * Created by Randolph Kahle on 3/27/14.
 */
class NetKernelTemplatePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.configurations.create("templates")
        project.tasks.create(name: 'listTemplateLibraries', group: 'Module Creation', type: ListTemplateLibrariesTask, description: "List available templates libraries.")
        project.tasks.create(name: 'listTemplates', group: 'Module Creation', type: ListTemplatesTask, description: "List available templates in a library.")
    }


}
