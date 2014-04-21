package org.netkernel.gradle.util

import jline.console.ConsoleReader
import jline.console.completer.Completer
import jline.console.completer.StringsCompleter
import spock.lang.Specification

import static org.netkernel.gradle.util.TemplateProperty.*

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
        File moduleDirectory = new File(TemplateHelperSpec.getResource("/test/workdir").file, "jarmodule")

        Templates templates = new Templates()
        templates.addFile(new File(TemplateHelperSpec.getResource("/test/template-library.jar").file))

        Map properties = [
            (MODULE_DESCRIPTION)      : 'Module Description',
            (MODULE_NAME)             : 'Module Name',
            (MODULE_SPACE_NAME)       : 'Space / Name',
            (MODULE_URN)              : 'urn:org:netkernel:test',
            (MODULE_URN_CORE)         : 'urn:org:netkernel',
            (MODULE_URN_CORE_PACKAGE) : 'org.netkernel',
            (MODULE_URN_RES_PATH)     : 'res:/org/netkernel/test',
            (MODULE_URN_RES_PATH_CORE): 'res:/org/netkernel',
            (MODULE_VERSION)          : '1.0.0',
            (MODULE_DIRECTORY)        : moduleDirectory
        ]

        when:
        templateHelper.buildModule(templates, "triad-core", properties)

        then:
        moduleDirectory.listFiles().size() > 0
    }

    def 'builds module from directory'() {
        setup:
        File moduleDirectory = new File(TemplateHelperSpec.getResource("/test/workdir").file, "directory")

        Templates templates = new Templates()
        templates.addDirectory(new File(TemplateHelperSpec.getResource("/test/templates").file))

        Map properties = [
            (MODULE_DESCRIPTION)      : 'Module Description',
            (MODULE_NAME)             : 'Module Name',
            (MODULE_SPACE_NAME)       : 'Space / Name',
            (MODULE_URN)              : 'urn:org:netkernel:test',
            (MODULE_URN_CORE)         : 'urn:org:netkernel',
            (MODULE_URN_CORE_PACKAGE) : 'org.netkernel',
            (MODULE_URN_RES_PATH)     : 'res:/org/netkernel/test',
            (MODULE_URN_RES_PATH_CORE): 'res:/org/netkernel',
            (MODULE_VERSION)          : '1.0.0',
            (MODULE_DIRECTORY)        : moduleDirectory
        ]

        when:
        templateHelper.buildModule(templates, "triad-core", properties)

        then:
        moduleDirectory.listFiles().size() > 0
    }

    def 'checks for binary file'() {
        when:
        boolean textFile = templateHelper.isText(new File(TemplateHelperSpec.getResource(file).file).getText('UTF-8'))

        then:
        textFile == expectedResult

        where:
        file | expectedResult
        '/test/files/file.txt' | true
        '/test/files/icon.png' | false
    }

    def 'cleans up path'() {
        when:
        String result = TemplateHelper.cleanupPath(path)

        then:
        result == expectedResult

        where:
        path | expectedResult
        '~/development' | "${System.getProperty('user.home')}/development"
    }

}
