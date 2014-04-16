package org.netkernel.gradle.plugin.tasks

import jline.console.completer.FileNameCompleter
import jline.console.completer.StringsCompleter
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.netkernel.gradle.util.TemplateHelper
import org.netkernel.gradle.util.Templates
import org.netkernel.gradle.util.URNHelper

import java.nio.file.Path
import java.nio.file.Paths

import static org.netkernel.gradle.util.TemplateProperty.*

/**
 * Created by Randolph Kahle on 3/27/14.
 */
class CreateModuleFromTemplateTask extends DefaultTask {

    TemplateHelper templateHelper = new TemplateHelper()
    URNHelper urnHelper = new URNHelper()
    Properties projectProperties

    @org.gradle.api.tasks.TaskAction
    void createNetKernelModules() {

        // We are going to check for properties to see if some or all of the required information is already provided
        ExtensionContainer e = project.getExtensions()
        ExtraPropertiesExtension ep = e.getExtraProperties()
        projectProperties = ep.getProperties()

        Map templateProperties = [
            (MODULE_DESCRIPTION): projectProperties.get(MODULE_DESCRIPTION),
            (MODULE_NAME)       : projectProperties.get(MODULE_NAME),
            (MODULE_SPACE_NAME) : projectProperties.get(MODULE_SPACE_NAME),
            (MODULE_URN)        : projectProperties.get(MODULE_URN),
            (MODULE_VERSION)    : projectProperties.get(MODULE_VERSION)
        ]

        Templates templates = new Templates()
        templates.loadTemplatesForProject(project)
        assert templates.size() > 0, 'No templates have been discovered from the declared dependencies or directories.'

        Path currentDirectory = Paths.get(".").toAbsolutePath().normalize()
        String destinationDirectory = templateHelper.promptForValue('Enter destination directory', currentDirectory.toString(), new FileNameCompleter())

        StringsCompleter templatesCompleter = new StringsCompleter(templates.names)
        String selectedTemplate = templateHelper.promptForValue('Enter the name of the template for this new module', null, templatesCompleter)
        assert templates.contains(selectedTemplate), "Could not find template: [${selectedTemplate}]"

        templateProperties[MODULE_URN] = templateProperties[MODULE_URN] ?: templateHelper.promptForValue('Enter the URN for the new module')

        File moduleDirectory = new File(destinationDirectory, urnHelper.urnToDirectoryName(templateProperties[MODULE_URN]))
        if (moduleDirectory.directory) {
            println "A module with the provided URN already exists"
            def yn = templateHelper.promptForValue('Do you want to replace this module with a newly created one from the template? (y/n)')
            if (yn.toLowerCase().equals('y')) {
                moduleDirectory.deleteDir()
                moduleDirectory.mkdirs()
            } else {
                return
            }
        }

        templateProperties[MODULE_DIRECTORY] = moduleDirectory

        templateProperties[MODULE_NAME] = templateProperties[MODULE_NAME] ?: templateHelper.promptForValue('Enter module name', 'Module Name')
        templateProperties[MODULE_DESCRIPTION] = templateProperties[MODULE_DESCRIPTION] ?: templateHelper.promptForValue('Enter module description', 'Module Description')
        templateProperties[MODULE_SPACE_NAME] = templateProperties[MODULE_SPACE_NAME] ?: templateHelper.promptForValue('Enter an ROC space name used in the Space explorer display for this module', 'Space / Name')
        templateProperties[MODULE_VERSION] = templateProperties[MODULE_VERSION] ?: templateHelper.promptForValue('Enter the version number', '1.0.0')

        // Update derived properties using module urnHelper
        templateProperties[MODULE_URN_CORE_PACKAGE] = urnHelper.urnToCorePackage(templateProperties[MODULE_URN])
        templateProperties[MODULE_URN_RES_PATH_CORE] = urnHelper.urnToResPath(urnHelper.urnToUrnCore(templateProperties[MODULE_URN]))
        templateProperties[MODULE_URN_RES_PATH] = urnHelper.urnToResPath(templateProperties[MODULE_URN])
        templateProperties[MODULE_URN_CORE] = urnHelper.urnToUrnCore(templateProperties[MODULE_URN])

        // Now we have the required information to create a module
        println "\nReady to build module with the following values:\n"
        templateProperties.each { key, value ->
            printf "%30s: %s\n", key, value
        }
        println ""

        def yn = templateHelper.promptForValue('Go ahead and build module (y/n)?')
        if ('y' == yn.toLowerCase()) {
            templateHelper.buildModule(templates, selectedTemplate, templateProperties)
            println "\nAll done. Your module is ready here:\n ${moduleDirectory.absolutePath}"
        }
    }

}