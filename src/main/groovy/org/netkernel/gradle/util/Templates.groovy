package org.netkernel.gradle.util

import groovy.util.logging.Slf4j

import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

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
                    doAddTemplate entry.name.split('/')[0], file
                }
            }
            zipFile.close()
        } catch (ZipException e) {
            log.warn "Could not open zip file: ${file}"
        }
    }

    void addDirectory(File directory) {
        directory.listFiles().findAll { it.directory }.each { File dir ->
            doAddTemplate dir.name, directory
        }
    }

    void addDirectories(String directories) {
        String[] dirs = directories.split(',').collect { it.trim() }
        dirs.each { directory ->
            // Replace references to '~/' with home directory
            String sanitizedDirectory = directory.trim().replaceAll('~/', "${System.getProperty("user.home")}/")
            File directoryFile = Paths.get(sanitizedDirectory).toAbsolutePath().toFile()
            addDirectory(directoryFile)
        }
    }

    boolean contains(String... templateNames) {
        return templates.keySet().containsAll(templateNames)
    }

    void doAddTemplate(String templateName, File source) {
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
            return rawSource.listFiles().find { it.name == templateName }
        }
        return templates[templateName]
    }

    int size() {
        return templates.keySet().size()
    }

}
