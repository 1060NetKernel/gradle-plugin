package org.netkernel.gradle.plugin.util

import groovy.util.slurpersupport.GPathResult

class ModuleHelper {

    GPathResult moduleInfo
    String moduleFilePath
    String version
    boolean versionOverridden = false

    ModuleHelper(String moduleFilePath) {
        this.moduleFilePath = moduleFilePath
        moduleInfo = new XmlSlurper().parse(moduleFilePath)
    }

    String getArchiveName() {
        return name + ".jar"
    }

    String getName() {
        return "${URIDotted}-${getVersion()}"
    }

    String getURIDotted() {
        return getURI().replaceAll(':', '.')
    }

    String getURI() {
        return moduleInfo.meta.identity.uri.text()
    }

    String getVersion() {
        if (version) {
            return version
        } else {
            return moduleInfo.meta.identity.version.text()
        }
    }

    void setVersion(String version) {
        this.version = version
        this.versionOverridden = true
    }
}