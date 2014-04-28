package org.netkernel.gradle.util

import groovy.util.slurpersupport.GPathResult

/**
 * A ModuleTemplate represents either a directory or jar source.  The config is loaded from a file
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
            config = new XmlSlurper().parse(new File(source, TEMPLATE_CONFIG))
        } else {
            // load config from jar here
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
