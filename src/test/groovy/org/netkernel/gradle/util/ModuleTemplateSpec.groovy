package org.netkernel.gradle.util

import spock.lang.Specification

class ModuleTemplateSpec extends Specification {

    def 'creates template'() {
        setup:
        File standardTemplateDir = new File(ModuleTemplatesSpec.getResource("/test/templates/standard").file)

        when:
        ModuleTemplate template = new ModuleTemplate(source: standardTemplateDir)

        then:
        template != null
        template.source == standardTemplateDir
        template.config != null
        template.config.properties != null
        template.config.properties.property[0].name == "moduleName"
        template.config.properties.property[0].default == "Module Name"
    }

    def 'gets qualified name'() {
        setup:
        File standardTemplateDir = new File(ModuleTemplatesSpec.getResource(source).file)
        ModuleTemplate moduleTemplate = new ModuleTemplate(name: 'template', source: standardTemplateDir)

        when:
        String qualifiedName = moduleTemplate.qualifiedName

        then:
        qualifiedName == expectedName

        where:
        source                       | expectedName
        "/test/templates/standard"   | 'template [..test/templates/]'
        "/test/template-library.jar" | 'template [template-library.jar]'
    }

}
