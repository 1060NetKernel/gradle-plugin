package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.netkernel.gradle.util.FileSystemHelper

import java.util.zip.ZipFile

class ListTemplateLibrariesTask extends DefaultTask {

    @org.gradle.api.tasks.TaskAction
    void listTemplateLibraries() {

        def templateName = ''
        Set<String> templateNames = new HashSet<String>()

        project.configurations.getByName('templates').dependencies.each {
            project.configurations.getByName('templates').fileCollection(it).each {
                println ''
                println '---- Template Library ----'
                File libraryJarFile = it
                println libraryJarFile.name
                def zipFile = new ZipFile(libraryJarFile)
                zipFile.entries().each {
                    if (it.directory && it.name.startsWith('modules') && it.name.length() > 'modules/'.length()) {
                        templateName = it.name.split('/')[1] // pick off the name of the directory
                        templateNames.add(templateName)
                    }
                }
                zipFile.close()
                templateNames.each { name ->
                    println name
                }
                templateNames = new HashSet<String>()
            }
        }
    }
}