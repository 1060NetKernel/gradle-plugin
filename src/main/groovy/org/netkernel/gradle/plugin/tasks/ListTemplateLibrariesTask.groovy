package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.util.ModuleTemplates

class ListTemplateLibrariesTask extends DefaultTask {

    @org.gradle.api.tasks.TaskAction
    void listTemplateLibraries() {
        ModuleTemplates templates = new ModuleTemplates()
        templates.loadTemplatesForProject(project)
        templates.listTemplates(System.out)

    }
}