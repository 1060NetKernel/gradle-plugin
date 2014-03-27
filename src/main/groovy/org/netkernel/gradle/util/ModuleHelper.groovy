package org.netkernel.gradle.util

class ModuleHelper {

    def moduleInfo

    def ModuleHelper(def moduleFile)
    {   moduleInfo = new XmlSlurper().parse(moduleFile)
    }

    def getModuleArchiveName() {
        return getModuleName() +".jar"
    }

    def getModuleName() {

        def moduleVersion = getModuleVersion()
        def fileName = getModuleURIDotted()

        return "${fileName}-${moduleVersion}"
    }

    def getModuleURIDotted()
    {   return getModuleURI().replaceAll(':', '.')
    }
    def getModuleURI()
    {   return moduleInfo.meta.identity.uri.text()
    }
    def getModuleVersion()
    {   return moduleInfo.meta.identity.version.text()
    }
}