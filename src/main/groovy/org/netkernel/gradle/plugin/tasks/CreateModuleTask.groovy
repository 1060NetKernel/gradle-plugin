package org.netkernel.gradle.plugin.tasks

import jline.console.completer.FileNameCompleter
import jline.console.completer.StringsCompleter
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.netkernel.gradle.plugin.util.ModuleTemplate
import org.netkernel.gradle.plugin.util.ModuleTemplates
import org.netkernel.gradle.plugin.util.TemplateHelper
import org.netkernel.gradle.plugin.util.TemplateProperties

/**
 * Creates new module(s) based on templates loaded from either a local directory or dependency declaration.
 *
 * @author Randolph Kahle on 3/27/14.
 */
class CreateModuleTask extends DefaultTask {

    TemplateHelper templateHelper = new TemplateHelper()
    Properties projectProperties

    @org.gradle.api.tasks.TaskAction
    void createNetKernelModules() {

        // We are going to check for properties to see if some or all of the required information is already provided
        ExtensionContainer e = project.getExtensions()
        ExtraPropertiesExtension ep = e.getExtraProperties()
        projectProperties = ep.getProperties()

        // TODO Come back to this and handle properties passed in from build or gradle.properties
//        TemplateProperties templateProperties = new TemplateProperties(properties: [
//            (MODULE_URN)        : projectProperties.get(MODULE_URN),
//        ])
        TemplateProperties templateProperties = new TemplateProperties()

        ModuleTemplates templates = new ModuleTemplates()
        templates.loadTemplatesForProject(project)
        assert templates.size() > 0, 'No templates have been discovered from the declared dependencies or directories.'

        File currentDirectory = new File(System.getProperty('user.dir'))

        String destinationDirectory = templateHelper.promptForValue('Enter destination base directory', currentDirectory.toString(), new FileNameCompleter())
        destinationDirectory = TemplateHelper.cleanupPath(destinationDirectory)
        templateProperties.destinationDirectory = new File(destinationDirectory)

        // Display templates
        templates.listTemplates(System.out)

        StringsCompleter templateNamesCompleter = new StringsCompleter(templates.qualifiedNames)
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

        if (moduleTemplate.config) {
            // Loop through additional properties and prompt user for values
            // TODO Look in properties first before prompting
            moduleTemplate.config.'properties'.'property'.each { property ->
                templateProperties[property.name.text()] = templateHelper.promptForValue(property.prompt.text(), property.default.text())
            }
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

}