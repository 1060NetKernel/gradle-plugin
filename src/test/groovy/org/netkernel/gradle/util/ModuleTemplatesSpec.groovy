package org.netkernel.gradle.util

import spock.lang.Specification

class ModuleTemplatesSpec extends Specification {

    ModuleTemplates templates = new ModuleTemplates()

    def "loads templates from classpath"() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)

        when:
        templates.addFile(templateFile)

        then:
        templates.names.containsAll('triad-core', 'triad-doc', 'triad-test')
    }

    def "doesn't add any templates from non jar file"() {
        setup:
        File textFile = new File(TemplateHelperSpec.getResource("/test/textfile.txt").file)

        when:
        templates.addFile(textFile)

        then:
        templates.size() == 0
    }

    def "loads templates from directory"() {
        setup:
        File templateDir = new File(TemplateHelperSpec.getResource("/test/templates").file)

        when:
        templates.addDirectory(templateDir)

        then:
        templates.contains('triad-core', 'triad-doc', 'triad-test')
    }

    def "doesn't add any templates for missing directory"() {
        when:
        templates.addDirectory(new File(UUID.randomUUID().toString()))

        then:
        templates.size() == 0
    }

    def "loads templates from multiple directories"() {
        setup:
        String templatesDir = TemplateHelperSpec.getResource("/test/templates").file
        String myTemplatesDir = TemplateHelperSpec.getResource("/test/mytemplates").file
        String directories = "${templatesDir}, ${myTemplatesDir}"

        when:
        templates.addDirectories(directories)

        then:
        templates.contains('triad-core', 'triad-doc', 'triad-test', 'cron-fulcrum', 'http-fulcrum')
    }

    def "gets templates source"() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File myTemplatesDir = new File(TemplateHelperSpec.getResource("/test/mytemplates").file)
        File cronFulcrumSource = new File(TemplateHelperSpec.getResource("/test/mytemplates/cron-fulcrum").file)

        when:
        templates.addFile(templateFile)
        templates.addDirectory(myTemplatesDir)

        then:
        templates.contains('triad-core', 'triad-doc', 'triad-test', 'cron-fulcrum', 'http-fulcrum')
        templates.getTemplate('triad-core').source == templateFile
        templates.getTemplate('cron-fulcrum').source == cronFulcrumSource
    }

    def "gets templates handling duplicates"() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File templatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        templates.addFile(templateFile)
        templates.addDirectory(templatesDir)

        when:
        ModuleTemplate template = templates.getTemplate('triad-core', templateFile)

        then:
        template.source == templateFile

        when:
        template = templates.getTemplate('triad-core', new File(templatesDir, "triad-core"))

        then:
        template.source == new File(templatesDir, 'triad-core')
    }

    def 'adds templates and handles duplicates'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File templatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        templates.addFile(templateFile)
        templates.addDirectory(templatesDir)

        when:
        def names = templates.names

        then:
        names.containsAll('triad-core', 'triad-core', 'triad-doc', 'triad-doc', 'triad-test', 'triad-test')
    }

    def 'gets qualified template names and handles duplicates'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File templatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        File templates2Dir = new File(TemplateHelperSpec.getResource("/test/templates2").file)
        templates.addFile(templateFile)
        templates.addDirectory(templatesDir)
        templates.addDirectory(templates2Dir)

        when:
        def qualifiedNames = templates.qualifiedNames

        then:
        qualifiedNames.containsAll(
            'triad-core [template-library.jar]',
            'triad-doc [template-library.jar]',
            'triad-test [template-library.jar]',
            'standard [..test/templates/]',
            'standard [..test/templates2/]',
            'triad-core [..test/templates/]',
            'triad-doc [..test/templates/]',
            'triad-test [..test/templates/]'
        )
    }

    def 'gets template by qualified name and handles duplicates'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File templatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        File templates2Dir = new File(TemplateHelperSpec.getResource("/test/templates2").file)
        templates.addFile(templateFile)
        templates.addDirectory(templatesDir)
        templates.addDirectory(templates2Dir)

        when:
        ModuleTemplate moduleTemplate = templates.getTemplateByQualifiedName(qualifiedName)

        then:
        moduleTemplate.name == name
        moduleTemplate.qualifiedName == qualifiedName

        where:
        qualifiedName                       | name
        'standard [..test/templates/]'      | 'standard'
        'standard [..test/templates2/]'     | 'standard'
        'triad-core [template-library.jar]' | 'triad-core'
    }

    def "doesn't add templates from non jar file"() {
        setup:
        File nonJarFile = new File(ModuleTemplatesSpec.getResource("/test/files/file.txt").file)

        when:
        templates.addFile(nonJarFile)

        then:
        templates.templates.size() == 0

    }

    def 'loads and retrieves template'() {
        setup:
        File templatesDir = new File(ModuleTemplatesSpec.getResource("/test/templates").file)

        when:
        templates.addDirectory(templatesDir)

        and:
        ModuleTemplate template = templates.getTemplate("standard")

        then:
        template != null
        template.source == new File(templatesDir, "standard")
        template.config != null
        template.config.properties != null
        template.config.properties.property[0].name == "moduleName"
        template.config.properties.property[0].default == "Module Name"
    }

}
