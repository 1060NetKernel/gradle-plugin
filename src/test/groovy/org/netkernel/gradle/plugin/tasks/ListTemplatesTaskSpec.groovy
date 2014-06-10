package org.netkernel.gradle.plugin.tasks

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.util.ModuleTemplates

class ListTemplatesTaskSpec extends BasePluginSpec {

    def 'lists templates'() {
        setup:
        ListTemplatesTask listTemplatesTask = createTask(ListTemplatesTask)
        ModuleTemplates mockModuleTemplates = Mock()

        // Using Groovy's mock interceptor because the ModuleTemplates is created in the task action method
        def mock = new MockFor(ModuleTemplates, true)
        mock.demand.with {
            ModuleTemplates() { mockModuleTemplates }
        }

        when:
        mock.use {
            listTemplatesTask.listTemplateLibraries()
        }

        then:
        1 * mockModuleTemplates.loadTemplatesForProject(_ as Project)
        1 * mockModuleTemplates.listTemplates(_ as OutputStream)
    }

}
