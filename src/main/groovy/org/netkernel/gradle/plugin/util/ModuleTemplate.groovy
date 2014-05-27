package org.netkernel.gradle.plugin.util

import groovy.util.slurpersupport.GPathResult

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * A ModuleTemplate represents either a directory or jar source.  An optional config is loaded from a file
 * called _template.xml in the base folder of the template.
 */
class ModuleTemplate {

    static final String TEMPLATE_CONFIG = "_template.xml"

    String name
    File source
    GPathResult config

    void setSource(File source) {
        this.source = source
        if (source.directory) {
            File configFile = new File(source, TEMPLATE_CONFIG)
            if(configFile.exists()) {
                config = new XmlSlurper().parse(configFile)
            }
        } else {
            ZipFile zipFile = new ZipFile(source)
            ZipEntry zipEntry = zipFile.getEntry("${name}/${TEMPLATE_CONFIG}")
            if(zipEntry) {
                config = new XmlSlurper().parse(zipFile.getInputStream(zipEntry))
            }
        }
    }

    String getQualifiedName() {
        String qualifiedName = source.name
        if(source.directory) {
            File parent = source.parentFile
            File grandparent = parent.parentFile

            qualifiedName = "..${grandparent.name}/${parent.name}/"
        }
        "${name} [${qualifiedName}]" as String
    }
}
