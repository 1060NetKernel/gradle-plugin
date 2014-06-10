package org.netkernel.gradle.plugin.model

import org.netkernel.gradle.plugin.BasePluginSpec

class ModuleSpec extends BasePluginSpec {

    def 'creates module'() {
        setup:
        File location = file '/test/sample-module.xml'
        String expectedVersion = '0.5.0'

        when:
        Module module = new Module(location)

        then:
        module.moduleFile == location
        module.version == expectedVersion as String
        module.URI == 'urn:org:netkernel:gradle:sample'
        module.URIDotted == 'urn.org.netkernel.gradle.sample'
        module.name == "urn.org.netkernel.gradle.sample-${expectedVersion}" as String
        module.archiveName == "urn.org.netkernel.gradle.sample-${expectedVersion}.jar" as String
    }

    def 'overrides version from module.xml'() {
        setup:
        File location = file '/test/sample-module.xml'
        String newVersion = '1.0.0'

        when:
        Module module = new Module(location)
        module.version = newVersion

        then:
        module.version == newVersion
        module.versionOverridden == true
    }

}
