package org.netkernel.gradle.util

import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import groovy.util.logging.Slf4j
import jline.console.ConsoleReader
import jline.console.completer.Completer

import java.util.zip.ZipFile

@Slf4j
class TemplateHelper {

    ConsoleReader consoleReader = new ConsoleReader(System.in, System.out)
    Configuration configuration

    TemplateHelper() {
        configuration = new Configuration()
        configuration.objectWrapper = new DefaultObjectWrapper()
        configuration.defaultEncoding = "UTF-8"
        configuration.templateExceptionHandler = TemplateExceptionHandler.IGNORE_HANDLER
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

    void buildModule(ModuleTemplate template, TemplateProperties templateProperties) {
        File destinationDirectory = templateProperties.destinationDirectory

        if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
            log.error "Could not create directory: ${destinationDirectory}"
        }

        if (template.source.directory) {
            buildModuleFromDirectory(template, templateProperties)
        } else {
            buildModuleFromJarFile(template, templateProperties)
        }

        configuration.clearTemplateCache()
    }

    void buildModuleFromJarFile(ModuleTemplate moduleTemplate, TemplateProperties templateProperties) {
        configuration.setClassForTemplateLoading(this.getClass(), "/")

        File destinationDirectory = templateProperties.destinationDirectory

        boolean startCopying = false
        ZipFile zipFile = new ZipFile(moduleTemplate.source)

        zipFile.entries().each { zipEntry ->
            if (zipEntry.name == "${moduleTemplate.name}/${ModuleTemplate.TEMPLATE_CONFIG}") {
                return
            }
            if (startCopying && zipEntry.name.startsWith(moduleTemplate.name)) {
                String templatePath = zipEntry.name.substring("${moduleTemplate.name}/".length())
                String destinationPath = getDestinationPath(templatePath, templateProperties)
                File outputFile = new File(destinationDirectory, destinationPath)
                if (zipEntry.directory) {
                    outputFile.mkdirs()
                } else {
                    String text = zipFile.getInputStream(zipEntry).text
                    if (zipEntry.name.endsWith('ftl') && isText(text)) {
                        Template template = new Template(zipEntry.name, text, configuration)
                        template.process(templateProperties.@templateProperties, outputFile.newWriter())
                    } else {
                        outputFile.bytes = zipFile.getInputStream(zipEntry).bytes
                    }
                }
            }
            if (!startCopying && zipEntry.directory && zipEntry.name.startsWith(moduleTemplate.name)) {
                startCopying = true
            }
        }
        zipFile.close()
    }

    void buildModuleFromDirectory(ModuleTemplate template, TemplateProperties templateProperties) {
        configuration.directoryForTemplateLoading = template.source

        File destinationDirectory = templateProperties.destinationDirectory

        template.source.eachFileRecurse { file ->
            // Don't copy _template.xml file over
            if (file.name == ModuleTemplate.TEMPLATE_CONFIG) {
                return
            }
            String templatePath = file.absolutePath - template.source.absolutePath
            String destinationPath = getDestinationPath(templatePath, templateProperties)
            File processedFile = new File(destinationDirectory, destinationPath)
            if (file.directory) {
                processedFile.mkdirs()
            } else if (file.name.endsWith('ftl') && isText(file.getText('UTF-8'))) {
                Template fmTemplate = configuration.getTemplate(templatePath)
                fmTemplate.process(templateProperties.@templateProperties, processedFile.newWriter())
            } else { // Just copy over the binary file
                processedFile.bytes = file.bytes
            }
        }
    }

    // TODO - Address the situation where there is a freemarker .ftl file with the same base name as a regular file (e.g. file.txt & file.txt.ftl)
    String getDestinationPath(String path, TemplateProperties templateProperties) {
        StringWriter writer = new StringWriter();
        new Template(path, path, configuration).process(templateProperties.@templateProperties, writer);

        // Strip off .ftl extension of template to get final name
        return writer.toString().replaceAll('\\.ftl$', '')
    }

    boolean isText(String text) {
        // Any non unicode character will return
        def nonCharacter = text.chars.find {
            !Character.isDefined(it.charValue())
        }
        return nonCharacter == null
    }

    // Replace references to '~/' with home directory
    static String cleanupPath(String path) {
        return path.trim().replaceAll('~/', "${System.getProperty("user.home")}/")
    }

}
