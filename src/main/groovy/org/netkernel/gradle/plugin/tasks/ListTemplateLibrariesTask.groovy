package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.util.Templates

class ListTemplateLibrariesTask extends DefaultTask {

    @org.gradle.api.tasks.TaskAction
    void listTemplateLibraries() {

        Templates templates = new Templates()
        templates.loadTemplatesForProject(project)

        println "- Templates Found ------"
        templates.templates.keySet().sort().each { println " ${it}" }
    }
}