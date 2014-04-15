package org.netkernel.gradle.util

import spock.lang.Specification

class TemplatesSpec extends Specification {

    Templates templates = new Templates()

    def "loads templates from classpath"() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)

        when:
        templates.addFile(templateFile)

        then:
        templates.names == ['triad-core', 'triad-doc', 'triad-test'] as Set
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
        templates.getTemplateSource('triad-core') == templateFile
        templates.getTemplateSource('cron-fulcrum') == cronFulcrumSource
    }

    def 'add template handles duplicates'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File templatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        templates.doAddTemplate("template", templateFile)
        templates.doAddTemplate("template", templatesDir)

        when:
        def names = templates.names

        then:
        names == ['template [template-library.jar]', 'template [templates/]'] as Set
    }

    def 'gets template names'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File myTemplatesDir = new File(TemplateHelperSpec.getResource("/test/mytemplates").file)
        templates.addFile(templateFile)
        templates.addDirectory(myTemplatesDir)

        when:
        def names = templates.names

        then:
        names == ['triad-core', 'triad-doc', 'triad-test', 'cron-fulcrum', 'http-fulcrum'] as Set

    }

    def 'handles duplicate template names loaded from file and directory'() {
        setup:
        File templateFile = new File(TemplateHelperSpec.getResource("/test/template-library.jar").file)
        File myTemplatesDir = new File(TemplateHelperSpec.getResource("/test/templates").file)
        templates.addFile(templateFile)
        templates.addDirectory(myTemplatesDir)

        when:
        def names = templates.names

        then:
        names == [
            'triad-core [template-library.jar]',
            'triad-core [templates/]',
            'triad-doc [template-library.jar]',
            'triad-doc [templates/]',
            'triad-test [template-library.jar]',
            'triad-test [templates/]'
        ] as Set
    }

    def 'adjusts template name'() {
        when:
        String templateName = templates.adjustTemplateName('template', source)

        then:
        templateName == expectedName

        where:
        source | expectedName
        new File(TemplateHelperSpec.getResource("/test/template-library.jar").file) | 'template [template-library.jar]'
        new File(TemplateHelperSpec.getResource("/test/templates").file)            | 'template [templates/]'
    }

}
