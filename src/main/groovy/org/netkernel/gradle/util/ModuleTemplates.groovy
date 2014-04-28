package org.netkernel.gradle.util

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

import static TemplateProperties.NETKERNEL_TEMPLATE_DIRS

@Slf4j
class ModuleTemplates {

    LinkedHashSet<ModuleTemplate> templates = [] as LinkedHashSet<ModuleTemplate>

    void addFile(File file) {
        // TODO: check to see if jar file exists and that it can be opened as a zip file
        try {
            ZipFile zipFile = new ZipFile(file)

            // Find all entries directly under modules
            zipFile.entries().findAll { it.name =~ /^[^\/]*\/$/ }.each { ZipEntry entry ->
                if (entry.directory && !entry.name.startsWith('META-INF')) {
                    templates << new ModuleTemplate(name: entry.name.split('/')[0], source: file)
                }
            }
            zipFile.close()
        } catch (ZipException e) {
            log.warn "Could not open zip file: ${file}"
        }
    }

    void addDirectory(File directory) {
        directory.listFiles().findAll { it.directory }.each { File dir ->
            templates << new ModuleTemplate(name: dir.name, source: dir)
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
        return templates.collect { it.name }.containsAll(templateNames)
    }


    Collection<String> getNames() {
        return templates.collect { it.name }
    }

    Collection<String> getQualifiedNames() {
        return templates.collect { it.qualifiedName }
    }

    ModuleTemplate getTemplate(String templateName, File source = null) {
        ModuleTemplate result

        Collection<ModuleTemplate> results = templates.findAll { it.name == templateName }

        if (results.size() == 1) {
            result = results.toArray()[0]
        } else if (results.size() > 1 && source) {
            result = results.find { it.source == source }
        }

        return result
    }

    ModuleTemplate getTemplateByQualifiedName(String qualifiedName) {
        return templates.find { it.qualifiedName == qualifiedName }
    }

    int size() {
        return templates.size()
    }

    void loadTemplatesForProject(Project project) {
        /**
         * Loads templates from both 'template' type dependencies and directories referenced in the netkernel.template.dirs
         * property in ~/.gradle/gradle.properties.
         */

        // Load any templates referenced by declared dependency
        project.configurations.getByName('templates').dependencies.each { Dependency dependency ->
            println dependency
            project.configurations.getByName('templates').fileCollection(dependency).each { jarFile ->
                addFile(jarFile)
            }
        }

        // Load any templates from netkernel.template.dirs system property
        if (project.property(NETKERNEL_TEMPLATE_DIRS as String)) {
            addDirectories(project.property(NETKERNEL_TEMPLATE_DIRS as String))
        }
    }

}
