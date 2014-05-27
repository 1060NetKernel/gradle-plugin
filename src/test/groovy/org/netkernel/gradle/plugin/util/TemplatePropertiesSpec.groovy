package org.netkernel.gradle.plugin.util

import spock.lang.Specification

class TemplatePropertiesSpec extends Specification {

    def 'augments properties'() {
        when:
        TemplateProperties templateProperties = new TemplateProperties(properties: [
            'moduleUrn'  : 'urn:org:netkernel:lang:groovy',
            'anotherUrn' : "urn:org:netkernel:lang:groovy:doc",
            'basePackage': 'org.netkernel.lang',
            'version'    : '0.0.1-SNAPSHOT'
        ])

        then:
        templateProperties.moduleUrnAsPath == 'urn.org.netkernel.lang.groovy'
        templateProperties.moduleUrnAsPackage == 'org.netkernel.lang.groovy'
        templateProperties.moduleUrnAsPackagePath == 'org/netkernel/lang/groovy'
        templateProperties.moduleUrnAsResourcePath == "res:/org/netkernel/lang/groovy"
        templateProperties.moduleUrnAsGroup == 'org.netkernel.lang'

        templateProperties.anotherUrnAsPath == "urn.org.netkernel.lang.groovy.doc"

        templateProperties.versionAsModuleXmlFriendlyVersion == "0.0.1"

        when:
        Set propertyNames = templateProperties.@templateProperties.keySet()

        then:
        propertyNames.size() == 15
        propertyNames == ['moduleUrn', 'anotherUrn', 'basePackage', 'version', 'moduleUrnAsPath', 'moduleUrnAsPackage', 'moduleUrnAsPackagePath', 'moduleUrnAsResourcePath', 'moduleUrnAsGroup', 'anotherUrnAsPath', 'anotherUrnAsPackage', 'anotherUrnAsPackagePath', 'anotherUrnAsResourcePath', 'anotherUrnAsGroup', 'versionAsModuleXmlFriendlyVersion'] as Set
    }

    def 'sets and gets property values'() {
        setup:
        TemplateProperties templateProperties = new TemplateProperties()

        when:
        templateProperties.valuea = "valuea"
        templateProperties.valueb = "valueb"

        then:
        templateProperties.valuea == "valuea"
        templateProperties.valueb == "valueb"
    }

}
