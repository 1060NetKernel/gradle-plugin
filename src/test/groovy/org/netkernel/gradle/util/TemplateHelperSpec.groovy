package org.netkernel.gradle.util

import jline.console.ConsoleReader
import jline.console.completer.Completer
import jline.console.completer.StringsCompleter
import spock.lang.Specification

import static org.netkernel.gradle.util.TemplateProperties.*

class TemplateHelperSpec extends Specification {

    static final String MODULE_NAME_VALUE = 'module name'

    TemplateHelper templateHelper
    ConsoleReader mockConsoleReader

    void setup() {
        mockConsoleReader = Mock()
        templateHelper = new TemplateHelper(consoleReader: mockConsoleReader)
    }

    def "gets value entered by user"() {
        when:
        String value = templateHelper.promptForValue('Enter value')

        then:
        1 * mockConsoleReader.readLine('Enter value: ') >> userEnteredString
        value == MODULE_NAME_VALUE

        where:
        // Make sure white space is handled properly
        userEnteredString << [
            MODULE_NAME_VALUE,
            "${MODULE_NAME_VALUE} ",
            "  ${MODULE_NAME_VALUE}  "
        ]
    }

    def 'gets value entered by user with completer'() {
        setup:
        Completer completer = new StringsCompleter()

        when:
        String value = templateHelper.promptForValue('Enter value', null, completer)

        then:
        1 * mockConsoleReader.addCompleter(completer)
        1 * mockConsoleReader.readLine('Enter value: ') >> MODULE_NAME_VALUE
        1 * mockConsoleReader.removeCompleter(completer)
        value == MODULE_NAME_VALUE
    }

    def "gets value using default value when no value is entered"() {
        when:
        String value = templateHelper.promptForValue('Enter value', MODULE_NAME_VALUE)

        then:
        1 * mockConsoleReader.readLine("Enter value [default: ${MODULE_NAME_VALUE}]: ") >> userEnteredString
        value == MODULE_NAME_VALUE

        where:
        userEnteredString << ['', '   ', '\t']

    }

    def 'builds module from jar file'() {
        setup:
        File destinationDirectory = new File(TemplateHelperSpec.getResource("/test/workdir").file, "moduleFromJarFile")

        ModuleTemplates templates = new ModuleTemplates()
        templates.addFile(new File(TemplateHelperSpec.getResource("/test/template-library.jar").file))

        ModuleTemplate template = templates.getTemplate('standard')

        TemplateProperties properties = new TemplateProperties(properties: [
            (MODULE_URN)           : 'urn:org:netkernel:test',
            (DESTINATION_DIRECTORY): destinationDirectory
        ])

        when:
        templateHelper.buildModule(template, properties)

        then:
        new File(destinationDirectory, "urn.org.netkernel.test/src/main/groovy/org/netkernel/test/SampleAccessor.groovy").exists()

//        and:
        // Make sure text file for no templates was not modified
//        new File(templateDirectory, "standard/\${moduleUrnAsPath}/${unmodifiedTextPath}").text == new File(destinationDirectory, "urn.org.netkernel.test/${unmodifiedTextPath}").text


        and: // make sure _template.xml was not copied over
        !(new File(destinationDirectory, "_template.xml").exists())
    }

    def 'builds module from directory'() {
        setup:
        File destinationDirectory = new File(TemplateHelperSpec.getResource("/test/workdir").file, "moduleFromDirectory")
        File templateDirectory = new File(TemplateHelperSpec.getResource("/test/templates").file)

        ModuleTemplates templates = new ModuleTemplates()
        templates.addDirectory(templateDirectory)

        ModuleTemplate template = templates.getTemplate("standard")

        TemplateProperties properties = new TemplateProperties(properties: [
            (MODULE_URN)           : 'urn:org:netkernel:test',
            (DESTINATION_DIRECTORY): destinationDirectory
        ])

        String unmodifiedTextPath = "src/main/resources/resources/unmodified.txt"

        when:
        templateHelper.buildModule(template, properties)

        then:
        new File(destinationDirectory, "urn.org.netkernel.test/src/main/groovy/org/netkernel/test/SampleAccessor.groovy").exists()

        and:
        // Make sure text file for no templates was not modified
        new File(templateDirectory, "standard/\${moduleUrnAsPath}/${unmodifiedTextPath}").text == new File(destinationDirectory, "urn.org.netkernel.test/${unmodifiedTextPath}").text

        and: // make sure _template.xml was not copied over
        !(new File(destinationDirectory, "_template.xml").exists())
    }

    def 'checks for binary file'() {
        when:
        boolean textFile = templateHelper.isText(new File(TemplateHelperSpec.getResource(file).file).getText('UTF-8'))

        then:
        textFile == expectedResult

        where:
        file                   | expectedResult
        '/test/files/file.txt' | true
        '/test/files/icon.png' | false
    }

    def 'cleans up path'() {
        when:
        String result = TemplateHelper.cleanupPath(path)

        then:
        result == expectedResult

        where:
        path            | expectedResult
        '~/development' | "${System.getProperty('user.home')}/development"
    }

    def 'gets destination path'() {
        setup:
        TemplateProperties properties = new TemplateProperties()
        properties.modulePath = "module"

        when:
        String updatedPath = templateHelper.getDestinationPath(path, properties)

        then:
        updatedPath == expectedPath

        where:
        path                                 | expectedPath
        '/root/${modulePath}/module.xml.ftl' | '/root/module/module.xml'
        '/root/${modulePath}/module.xml'     | '/root/module/module.xml'
        '/root/${modulePath}/${modulePath}'  | '/root/module/module'
    }

}
