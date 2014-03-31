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
        def templateOptions = []

        def urnHelper = new URNHelper()

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        // We are going to check for properties to see if some or all of the required information is already provided
        ExtensionContainer e = project.getExtensions()
        ExtraPropertiesExtension ep = e.getExtraProperties()
        def properties = ep.getProperties()


        project.configurations.getByName('templates').dependencies.each {
            project.configurations.getByName('templates').fileCollection(it).each {
                println '---- Template Library ----'
                File libraryJarFile = it
                println libraryJarFile.name
                def zipFile = new ZipFile(libraryJarFile)
                zipFile.entries().each {
                    if (it.directory && it.name.startsWith('modules') && it.name.length() > 9) {
                        templateName = it.name.split('/')[1]
                        println templateName
                        templateOptions.add(templateName)
                        templateExists = true
                    }
                }
                zipFile.close()
            }
        }


        if (!templateExists) {
            println 'No templates have been discovered from the declared dependencies.'
            return
        } else {
            println 'Enter the name of the template for this new module:'
            templateName = br.readLine()
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

        // Our new module directory is created as is empty

        // We need to search all dependent JAR files to find the modules/{template-name} specified by the user

        def startCopying = false
        project.configurations.getByName('templates').dependencies.each {
            project.configurations.getByName('templates').fileCollection(it).each {
                File libraryJarFile = it
                println libraryJarFile.name
                def zipFile = new ZipFile(libraryJarFile)
                zipFile.entries().each { zipEntry ->
                    if (startCopying && zipEntry.name.startsWith("modules/$templateName")) {
                        println "Copying ..."
                        if (zipEntry.directory) {
                            def translatedDirectory = "${urnHelper.urnToDirectoryName(moduleURN)}/${zipEntry.name.substring("modules/$templateName/".length())}"
                            println translatedDirectory
                            project.file(translatedDirectory).mkdirs()
                        } else {
                            def translatedFile = "${urnHelper.urnToDirectoryName(moduleURN)}/${zipEntry.name.substring("modules/$templateName/".length())}"
                            def fileContents = zipFile.getInputStream(zipEntry).text
                            println fileContents
                            String template = fileContents
                            template = template.replaceAll("MODULE_DESCRIPTION","Module description")
                            template = template.replaceAll("MODULE_VERSION","1.0.0")
                            template = template.replaceAll("MODULE_URN_RES_PATH_CORE", urnHelper.urnToResPath(urnHelper.urnToUrnCode(moduleURN)))
                            template = template.replaceAll("MODULE_URN_RES_PATH", urnHelper.urnToResPath(moduleURN))
                            template = template.replaceAll("MODULE_URN_CORE", urnHelper.urnToUrnCode(moduleURN))
                            template = template.replaceAll("MODULE_URN", moduleURN)
                            println template
                            project.file(translatedFile) << template
                        }

                    }
                    if (!startCopying && zipEntry.directory && zipEntry.name.startsWith("modules/$templateName")) {
                        startCopying = true
                        println 'Start Copying...'
                    }
                }
                zipFile.close()
            }
        }

//
//        // Get user supplied template directory or use default
//        String templateDirectory = /*project.*/fsHelper.gradleHomeDir() + '/netkernelroc/templates'
//        if (properties.containsKey('templateDirectory')) {
//            templateDirectory = properties['templateDirectory']
//        }
//
//        if (!/*project.*/fsHelper.dirExists/*existsDir*/(templateDirectory)) {
//            println "The specified template directory [${templateDirectory}] does not exist."
//        }
//        else {
//            println ""
//            println "Directory: ${templateDirectory}"
//            String templateLibraryDirectory = templateDirectory + '/' + templateLibrary
//            if (!/*project.*/fsHelper.dirExists/*.existsDir*/(templateLibraryDirectory)){
//                println "The template Libary [${templateLibrary}] is not found in the template diretory"
//            }
//            else {
//                println ""
//                println "Library: ${templateLibrary}"
//                def tlDir = new File(templateLibraryDirectory)
//                tlDir.eachFile { file ->
//                    if (file.name.toLowerCase().equals("readme")) {
//                        println file.text
//                    }
//                }
//                tlDir.eachDir { dir ->
//                    println "Template: ${dir.name}"
//                    println "-----------------------------"
//                    dir.eachFile { file ->
//                        if (file.name.toLowerCase().equals("readme")) {
//                            String readme = file.text
//                            println readme
//                            println  ""
//                        }
//                    }
//                }
//            }
//        }
    }


}
