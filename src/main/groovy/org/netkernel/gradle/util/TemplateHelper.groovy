package org.netkernel.gradle.util

import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import freemarker.template.Template
import jline.console.ConsoleReader
import jline.console.completer.Completer

import java.util.zip.ZipFile

class TemplateHelper {

    static final String NETKERNEL_TEMPLATE_DIRS = "netkernel.template.dirs"

    ConsoleReader consoleReader = new ConsoleReader(System.in, System.out)
    Configuration configuration

    TemplateHelper() {
        configuration = new Configuration()
        configuration.objectWrapper = new DefaultObjectWrapper()
        configuration.defaultEncoding = "UTF-8"
    }

    String promptForValue(String prompt, String defaultValue = null, Completer completer = null) {
        String value

        completer && consoleReader.addCompleter(completer)

        // Determine prompt to display.  Default values are added in () to the prompt
        String displayedPrompt = prompt
        if (defaultValue) {
            displayedPrompt += " [default: ${defaultValue}]"
        }
        displayedPrompt = "${displayedPrompt}: "

        value = consoleReader.readLine displayedPrompt
        completer && consoleReader.removeCompleter(completer)

        value = value?.trim()

        // If no value was entered, use the default value if supplied
        if (!value && defaultValue) {
            value = defaultValue
        }

        return value
    }

    void buildModule(Templates templates, String selectedTemplate, Map properties) {
        File source = templates.getTemplateSource(selectedTemplate)
        File moduleDirectory = properties[TemplateProperty.MODULE_DIRECTORY]

        moduleDirectory.mkdirs() // TODO, check response value

        if (source.directory) {
            buildModuleFromDirectory(moduleDirectory, source, properties)
        } else {
            buildModuleFromJarFile(moduleDirectory, source, selectedTemplate, properties)
        }
    }

    void buildModuleFromJarFile(File moduleDirectory, File source, String selectedTemplate, Map properties) {
        // Search all dependent JAR files to find the modules/{template-name} specified by the user
        boolean startCopying = false
        ZipFile zipFile = new ZipFile(source)

        configuration.setClassForTemplateLoading(this.getClass(), "/")

        zipFile.entries().each { zipEntry ->
            if (startCopying && zipEntry.name.startsWith(selectedTemplate)) {
                String newPath = zipEntry.name.substring("$selectedTemplate/".length())
                File outputFile = new File(moduleDirectory, newPath)
                if (zipEntry.directory) {
                    outputFile.mkdirs()
                } else {
                    String text = zipFile.getInputStream(zipEntry).text
                    if (isText(text)) {
                        Template template = new Template(zipEntry.name, text, configuration)
                        template.process(properties, outputFile.newWriter())
                    } else {
                        outputFile.bytes = text.bytes
                    }
                }
            }
            if (!startCopying && zipEntry.directory && zipEntry.name.startsWith(selectedTemplate)) {
                startCopying = true
            }
        }
        zipFile.close()

        configuration.clearTemplateCache()
    }

    void buildModuleFromDirectory(File moduleDirectory, File source, Map properties) {

        configuration.directoryForTemplateLoading = source

        source.eachFileRecurse { file ->
            String newPath = file.absolutePath - source.absolutePath
            File processedFile = new File(moduleDirectory, newPath)
            if (file.directory) {
                processedFile.mkdirs()
            } else if (isText(file.getText('UTF-8'))) {
                Template template = configuration.getTemplate(newPath)
                template.process(properties, processedFile.newWriter())
            } else { // Just copy over the binary file
                processedFile.bytes = file.bytes
            }
        }

        configuration.clearTemplateCache()
    }

    boolean isText(String text) {
        // Any non unicode character will return
        def nonCharacter = text.chars.find {
            !Character.isDefined(it.charValue())
        }
        return nonCharacter == null
    }
}
