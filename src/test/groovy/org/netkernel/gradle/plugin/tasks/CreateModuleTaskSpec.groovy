package org.netkernel.gradle.plugin.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.util.ModuleTemplate
import org.netkernel.gradle.plugin.util.TemplateHelper
import org.netkernel.gradle.plugin.util.TemplateProperties
import spock.lang.Specification

class CreateModuleTaskSpec extends Specification {

    CreateModuleTask createModuleFromTemplate
    TemplateHelper mockTemplateHelper

    void setup() {
        mockTemplateHelper = Mock()
        String templateDir = CreateModuleTaskSpec.getResource("/test/templates").file

        Project project = ProjectBuilder.builder().build()
        project.setProperty(TemplateProperties.NETKERNEL_TEMPLATE_DIRS, templateDir)
        project.configurations.create("templates")
        createModuleFromTemplate = project.tasks.create(name: 'cmft', type: CreateModuleTask)
        createModuleFromTemplate.templateHelper = mockTemplateHelper
    }

    def 'creates new module'() {
        setup:
        File workDir = new File(CreateModuleTaskSpec.getResource("/test/workdir").file)

        when:
        createModuleFromTemplate.execute()

        then:
        1 * mockTemplateHelper.promptForValue('Enter destination base directory', _, _) >> workDir.absolutePath
        1 * mockTemplateHelper.promptForValue('Enter the name of the template for this new module', _, _) >> "standard [..test/templates/]"
        1 * mockTemplateHelper.promptForValue('Enter the URN for the new module') >> "urn:org:netkernel:test"
        1 * mockTemplateHelper.promptForValue('Enter module name', 'Module name') >> 'Module name'
        1 * mockTemplateHelper.promptForValue('Enter module description', 'Module description') >> 'Module description'
        1 * mockTemplateHelper.promptForValue('Enter module space name', 'Space / Name') >> 'Space / Name'
        1 * mockTemplateHelper.promptForValue('Enter module version', '0.0.1-SNAPSHOT') >> '0.0.1-SNAPSHOT'
        1 * mockTemplateHelper.promptForValue('Go ahead and build module (y/n)?') >> 'y'
        1 * mockTemplateHelper.buildModule(_ as ModuleTemplate, _ as TemplateProperties) >> { ModuleTemplate template, TemplateProperties templateProperties ->
            assert template.name == "standard"
            assert templateProperties.moduleDescription == "Module description"
            assert templateProperties.moduleName == 'Module name'
            assert templateProperties.moduleSpaceName == 'Space / Name'
            assert templateProperties.moduleUrn == 'urn:org:netkernel:test'
            assert templateProperties.moduleVersion == '0.0.1-SNAPSHOT'
            assert templateProperties.moduleVersionAsModuleXmlFriendlyVersion == "0.0.1"
            assert templateProperties.destinationDirectory == workDir
            assert templateProperties.moduleUrnAsPath == 'urn.org.netkernel.test'
            assert templateProperties.moduleUrnAsPackage == 'org.netkernel.test'
            assert templateProperties.moduleUrnAsPackagePath == 'org/netkernel/test'
            assert templateProperties.moduleUrnAsResourcePath == 'res:/org/netkernel/test'
            assert templateProperties.moduleUrnAsGroup == 'org.netkernel'

        }
    }

    def 'creates new module for template that has no configuration'() {
        setup:
        File workDir = new File(CreateModuleTaskSpec.getResource('/test/workdir').file)

        when:
        createModuleFromTemplate.execute()

        then:
        1 * mockTemplateHelper.promptForValue('Enter destination base directory', _, _) >> workDir.absolutePath
        1 * mockTemplateHelper.promptForValue('Enter the name of the template for this new module', _, _) >> "triad-test [..test/templates/]"
        1 * mockTemplateHelper.promptForValue('Enter the URN for the new module') >> "urn:org:netkernel:test"
        1 * mockTemplateHelper.promptForValue('Go ahead and build module (y/n)?') >> 'y'
        1 * mockTemplateHelper.buildModule(_ as ModuleTemplate, _ as TemplateProperties) >> { ModuleTemplate template, TemplateProperties templateProperties ->
            assert template.name == "triad-test"
            assert templateProperties.moduleUrn == 'urn:org:netkernel:test'
            assert templateProperties.destinationDirectory == workDir
            assert templateProperties.moduleUrnAsPath == 'urn.org.netkernel.test'
            assert templateProperties.moduleUrnAsPackage == 'org.netkernel.test'
            assert templateProperties.moduleUrnAsPackagePath == 'org/netkernel/test'
            assert templateProperties.moduleUrnAsResourcePath == 'res:/org/netkernel/test'
            assert templateProperties.moduleUrnAsGroup == 'org.netkernel'

        }
    }


}
