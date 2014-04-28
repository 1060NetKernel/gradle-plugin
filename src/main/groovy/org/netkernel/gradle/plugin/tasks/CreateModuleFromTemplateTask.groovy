package org.netkernel.gradle.plugin.tasks

import jline.console.completer.FileNameCompleter
import jline.console.completer.StringsCompleter
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.netkernel.gradle.util.ModuleTemplate
import org.netkernel.gradle.util.ModuleTemplates
import org.netkernel.gradle.util.TemplateHelper
import org.netkernel.gradle.util.TemplateProperties
import org.netkernel.gradle.util.URNHelper

import static org.netkernel.gradle.util.TemplateProperties.*

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

        TemplateProperties templateProperties = new TemplateProperties(properties: [
            (MODULE_DESCRIPTION): projectProperties.get(MODULE_DESCRIPTION),
            (MODULE_NAME)       : projectProperties.get(MODULE_NAME),
            (MODULE_SPACE_NAME) : projectProperties.get(MODULE_SPACE_NAME),
            (MODULE_URN)        : projectProperties.get(MODULE_URN),
            (MODULE_VERSION)    : projectProperties.get(MODULE_VERSION)
        ])

        ModuleTemplates templates = new ModuleTemplates()
        templates.loadTemplatesForProject(project)
        assert templates.size() > 0, 'No templates have been discovered from the declared dependencies or directories.'

        // Dump templates
        templates.templates.each { ModuleTemplate template ->
            println "${template.name} -> ${template.source}"
        }

        File currentDirectory = new File(System.getProperty('user.dir'))

        String destinationDirectory = templateHelper.promptForValue('Enter destination base directory', currentDirectory.toString(), new FileNameCompleter())
        destinationDirectory = TemplateHelper.cleanupPath(destinationDirectory)
        templateProperties.destinationDirectory = new File(destinationDirectory)

        TemplateNamesCompleter templateNamesCompleter = new TemplateNamesCompleter(templates)
        String qualifiedTemplateName = templateHelper.promptForValue('Enter the name of the template for this new module', null, templateNamesCompleter)
        ModuleTemplate moduleTemplate = templates.getTemplateByQualifiedName(qualifiedTemplateName)
        assert moduleTemplate, "Could not find template: [${qualifiedTemplateName}]"

        templateProperties.moduleUrn = templateProperties.moduleUrn ?: templateHelper.promptForValue('Enter the URN for the new module')

//        File moduleDirectory = new File(destinationDirectory, urnHelper.urnToDirectoryName(templateProperties[MODULE_URN]))
//        if (moduleDirectory.directory) {
//            println "A module with the provided URN already exists"
//            def yn = templateHelper.promptForValue('Do you want to replace this module with a newly created one from the template? (y/n)')
//            if (yn.toLowerCase().equals('y')) {
//                moduleDirectory.deleteDir()
//                moduleDirectory.mkdirs()
//            } else {
//                return
//            }
//        }

        // Loop through template properties and prompt user for missing ones
        moduleTemplate.config.'properties'.'property'.each { property ->
            templateProperties[property.name.text()] = templateHelper.promptForValue(property.prompt.text(), property.default.text())
        }

        // Now we have the required information to create a module
        println "\nReady to build module with the following values:\n"
        templateProperties.each { key, value ->
            printf "%40s: %s\n", key, value
        }

        def yn = templateHelper.promptForValue('Go ahead and build module (y/n)?')
        if ('y' == yn.toLowerCase()) {
            templateHelper.buildModule(moduleTemplate, templateProperties)
            println "\nAll done. Your module is ready here:\n ${destinationDirectory}"
        }
    }

    static class TemplateNamesCompleter extends StringsCompleter {

        TemplateNamesCompleter(ModuleTemplates templates) {
            super(templates.qualifiedNames)
        }

    }

}