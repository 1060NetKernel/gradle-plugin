package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.netkernel.gradle.util.URNHelper

import java.util.zip.ZipFile

/**
 * Created by randolph.kahle on 3/27/14.
 */
class CreateNetKernelModuleFromTemplate extends DefaultTask {


    @org.gradle.api.tasks.TaskAction
    void createNetKernelModules() {

        def templateExists = false
        def templateName = ''
        def moduleURN = ''
        def moduleName = ''
        def moduleDescription = ''
        def moduleSpaceName = ''
        def moduleVersion = ''
        def templateOptions = []
        String userInput = ''

        def urnHelper = new URNHelper()

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        // We are going to check for properties to see if some or all of the required information is already provided
        ExtensionContainer e = project.getExtensions()
        ExtraPropertiesExtension ep = e.getExtraProperties()
        def properties = ep.getProperties()

        Set<String> templateNames = new HashSet<String>()

        project.configurations.getByName('templates').dependencies.each {
            project.configurations.getByName('templates').fileCollection(it).each {
                println '---- Template Library ----'
                File libraryJarFile = it
                println libraryJarFile.name
                def zipFile = new ZipFile(libraryJarFile)
                zipFile.entries().each {
                    if (it.directory && it.name.startsWith('modules') && it.name.length() > 9) {
                        templateName = it.name.split('/')[1]
                        templateNames.add(templateName)
                        templateOptions.add(templateName)
                        templateExists = true
                    }
                }
                zipFile.close()
                templateNames.each { name ->
                    println name
                }
                templateNames.clear()
                templateNames = new HashSet<String>()
            }
        }



        if (!templateExists) {
            println 'No templates have been discovered from the declared dependencies.'
            return
        } else {

            // Get user supplied template name or ask user with a prompt
            if (properties.containsKey('templateName')){
                templateName = properties['templateName']
            } else {
                println 'Enter the name of the template for this new module:'
                templateName = br.readLine()
            }

            println ""
            if (templateOptions.contains(templateName)) {
                println "The template you selected is: $templateName"
            } else {
                println "The template name you selected ($templateName) does not exist"
                return
            }

            // Get user supplied module URN or ask user with a prompt
            if (properties.containsKey('moduleURN')) {
                moduleURN = properties['moduleURN']
                println "The module URN specified as a property is: $moduleURN"
            } else {
                println 'Enter the URN for the new module:'
                moduleURN = br.readLine()
                println "The URN for the new module is: $moduleURN"
            }

            // Get user supplied module name or ask user with a prompt
            moduleName = 'Module Name'
            if (properties.containsKey('moduleName')){
                userInput = properties['moduleName']
            } else {
                println "Enter a name for this module ($moduleName): "
                userInput = br.readLine()
            }
            moduleName = userInput.length() == 0 ? moduleName : userInput

            // Get user supplied module description or ask user with a prompt
            moduleDescription = 'Module description'
            if (properties.containsKey('moduleDescription')){
                userInput = properties['moduleDescription']
            } else {
                println "Enter a description for this module ($moduleDescription):"
                userInput = br.readLine()
            }
            moduleDescription = userInput.length() == 0 ? moduleDescription : userInput

            // Get user supplied module space name or ask user with a prompt
            moduleSpaceName = 'Space / Name'
            if (properties.containsKey('moduleSpaceName')){
                userInput = properties['moduleSpaceName']
            } else {
                println "Enter an ROC space name used in the Space explorer display for this module ($moduleSpaceName):"
                userInput = br.readLine()
            }
            moduleSpaceName = userInput.length() == 0 ? moduleSpaceName : userInput

            // Get user supplied module version or ask user with a prompt
            moduleVersion = '1.0.0'
            if (properties.containsKey('moduleVersion')){
                userInput = properties['moduleVersion']
            } else {
                println "Enter the version number of the module ($moduleVersion):"
                userInput = br.readLine()
            }
            moduleVersion = userInput.length() == 0 ? moduleVersion : userInput

        }

        // Now we have the required information to create a module

        File moduleDirectory = project.file(urnHelper.urnToDirectoryName(moduleURN))
        if (moduleDirectory.directory) {
            println "A module with the provided URN already exists"
            println "Do you want to replace this module with a newly created one from the template? (y/n)"
            def yn = br.readLine()
            if (yn.toLowerCase().equals('y')) {
                moduleDirectory.deleteDir()
            } else {
                return
            }
        }

        moduleDirectory.mkdirs()

        // Search all dependent JAR files to find the modules/{template-name} specified by the user
        def startCopying = false
        project.configurations.getByName('templates').dependencies.each {
            project.configurations.getByName('templates').fileCollection(it).each {
                File libraryJarFile = it
                def zipFile = new ZipFile(libraryJarFile)
                zipFile.entries().each { zipEntry ->
                    if (startCopying && zipEntry.name.startsWith("modules/$templateName")) {
                        if (zipEntry.directory) {
                            def translatedDirectory = "${urnHelper.urnToDirectoryName(moduleURN)}/${zipEntry.name.substring("modules/$templateName/".length())}"
                            project.file(translatedDirectory).mkdirs()
                        } else {
                            def translatedFile = "${urnHelper.urnToDirectoryName(moduleURN)}/${zipEntry.name.substring("modules/$templateName/".length())}"
                            def fileContents = zipFile.getInputStream(zipEntry).text
                            String template = fileContents
                            template = template.replaceAll("MODULE_SPACE_NAME", moduleSpaceName)
                            template = template.replaceAll("MODULE_DESCRIPTION", moduleDescription)
                            template = template.replaceAll("MODULE_VERSION", moduleVersion)
                            template = template.replaceAll("MODULE_NAME", moduleName)
                            template = template.replaceAll("MODULE_URN_CORE_PACKAGE", urnHelper.urnToCorePackage(moduleURN))
                            template = template.replaceAll("MODULE_URN_RES_PATH_CORE", urnHelper.urnToResPath(urnHelper.urnToUrnCore(moduleURN)))
                            template = template.replaceAll("MODULE_URN_RES_PATH", urnHelper.urnToResPath(moduleURN))
                            template = template.replaceAll("MODULE_URN_CORE", urnHelper.urnToUrnCore(moduleURN))
                            template = template.replaceAll("MODULE_URN", moduleURN)
                            project.file(translatedFile) << template
                        }

                    }
                    if (!startCopying && zipEntry.directory && zipEntry.name.startsWith("modules/$templateName")) {
                        startCopying = true
                    }
                }
                zipFile.close()
            }
        }
    }
}
