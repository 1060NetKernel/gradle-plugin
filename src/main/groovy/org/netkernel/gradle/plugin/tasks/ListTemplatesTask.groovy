package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.plugin.util.ModuleTemplates

class ListTemplatesTask extends DefaultTask {

    @org.gradle.api.tasks.TaskAction
    void listTemplateLibraries() {
        ModuleTemplates templates = new ModuleTemplates()
        templates.loadTemplatesForProject(project)
        templates.listTemplates(System.out)

    }
}