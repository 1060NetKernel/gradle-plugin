package org.netkernel.gradle.util

import spock.lang.Specification

class ModuleHelperSpec extends Specification {

    def 'creates module helper'() {
        setup:
        String location = ModuleHelperSpec.getResource('/test/sample-module.xml').file
        String expectedVersion = '0.5.0'

        when:
        ModuleHelper moduleHelper = new ModuleHelper(location)

        then:
        moduleHelper.moduleFilePath == location
        moduleHelper.version == expectedVersion as String
        moduleHelper.URI == 'urn:org:netkernel:gradle:sample'
        moduleHelper.URIDotted == 'urn.org.netkernel.gradle.sample'
        moduleHelper.name == "urn.org.netkernel.gradle.sample-${expectedVersion}" as String
        moduleHelper.archiveName == "urn.org.netkernel.gradle.sample-${expectedVersion}.jar" as String
    }

    def 'overrides version from module.xml'() {
        setup:
        String location = ModuleHelperSpec.getResource('/test/sample-module.xml').file
        String newVersion = '1.0.0'

        when:
        ModuleHelper moduleHelper = new ModuleHelper(location)
        moduleHelper.version = newVersion

        then:
        moduleHelper.version == newVersion
        moduleHelper.versionOverridden == true
    }

}
