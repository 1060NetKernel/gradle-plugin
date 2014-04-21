package org.netkernel.gradle.util

import groovy.util.logging.Slf4j
import org.gradle.api.Project

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

import static org.netkernel.gradle.util.TemplateProperty.NETKERNEL_TEMPLATE_DIRS

@Slf4j
class Templates {

    Map templates = [:]

    void addFile(File file) {
        // TODO: check to see if jar file exists and that it can be opened as a zip file
        try {
            ZipFile zipFile = new ZipFile(file)

            // Find all entries directly under modules
            zipFile.entries().findAll { it.name =~ /^[^\/]*\/$/ }.each { ZipEntry entry ->
                if (entry.directory && !entry.name.startsWith('META-INF')) {
                    doAddTemplateSource entry.name.split('/')[0], file
                }
            }
            zipFile.close()
        } catch (ZipException e) {
            log.warn "Could not open zip file: ${file}"
        }
    }

    void addDirectory(File directory) {
        directory.listFiles().findAll { it.directory }.each { File dir ->
            doAddTemplateSource dir.name, directory
        }
    }

    void addDirectories(String directories) {
        String[] dirs = directories.split(',').collect { it.trim() }
        dirs.each { directory ->
            String sanitizedDirectory = TemplateHelper.cleanupPath(directory)
            File directoryFile = new File(sanitizedDirectory)
            addDirectory(directoryFile)
        }
    }

    boolean contains(String... templateNames) {
        return templates.keySet().containsAll(templateNames)
    }

    String adjustTemplateName(String templateName, File source) {
        String adjustedTemplateName = "${templateName}"
        adjustedTemplateName += " [${source.name}${source.directory ? '/' : ''}]"
        return adjustedTemplateName
    }

    Collection getNames() {
        return templates.keySet()
    }

    File getTemplateSource(String templateName) {
        File rawSource = templates[templateName]
        if (rawSource.directory) {
            String dirName = templateName.replaceAll(' \\[.*?\\]$','')
            return rawSource.listFiles().find { it.name == dirName }
        }
        return templates[templateName]
    }

    int size() {
        return templates.keySet().size()
    }

    void loadTemplatesForProject(Project project) {
        /**
         * Loads templates from both 'template' type dependencies and directories referenced in the netkernel.template.dirs
         * property in ~/.gradle/gradle.properties.
         */

        // Load any templates referenced by declared dependency
        project.configurations.getByName('templates').dependencies.each { dependency ->
            project.configurations.getByName('templates').fileCollection(dependency).each { jarFile ->
                addFile(jarFile)
            }
        }

        // Load any templates from netkernel.template.dirs system property
        if (project.property(NETKERNEL_TEMPLATE_DIRS as String)) {
            addDirectories(project.property(NETKERNEL_TEMPLATE_DIRS as String))
        }

    }

    private void doAddTemplateSource(String templateName, File source) {
        if (templates[templateName]) {

            File originalSource = templates.remove templateName
            String adjustedOriginalName = adjustTemplateName(templateName, originalSource)
            String adjustedTemplateName = adjustTemplateName(templateName, source)

            templates[adjustedOriginalName] = originalSource
            templates[adjustedTemplateName] = source
        } else {
            templates[templateName] = source
        }
    }

}
