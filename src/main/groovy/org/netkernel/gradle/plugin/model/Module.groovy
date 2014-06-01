package org.netkernel.gradle.plugin.model

import groovy.util.slurpersupport.GPathResult

class Module {

    File moduleFile
    GPathResult moduleInfo
    String version
    boolean versionOverridden = false

    Module(File moduleFile) {
        this.moduleFile = moduleFile
        moduleInfo = new XmlSlurper().parse(moduleFile)
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
