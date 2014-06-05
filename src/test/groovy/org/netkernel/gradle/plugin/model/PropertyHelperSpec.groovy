package org.netkernel.gradle.plugin.model

import org.netkernel.gradle.plugin.BasePluginSpec

class PropertyHelperSpec extends BasePluginSpec {

    PropertyHelper propertyHelper

    void setup() {
        propertyHelper = new PropertyHelper()
    }

    def 'gets property value from project'() {
        setup:
        project.properties.sort().each { p -> println p }

        when:
        String value = propertyHelper.findProjectProperty(project, 'name', 'default')

        then:
        value == 'test'

    }

    def 'gets property value from system properties'() {
        setup:
        String propertyName = 'gradleTestProperty'
        String propertyValue = 'value'
        System.setProperty(propertyName, propertyValue)

        when:
        String result = propertyHelper.findProjectProperty(project, propertyName, null)

        then:
        result == propertyValue

        cleanup:
        System.clearProperty(propertyName)

    }

    def 'gets property value using default value'() {
        setup:
        String defaultValue = 'defaultValue'

        when:
        String result = propertyHelper.findProjectProperty(project, 'propertyName', defaultValue)

        then:
        result == defaultValue
    }

    def 'gets property from property file included in plugin'() {
        setup:

        when:
        String result = propertyHelper.findProjectProperty(project, 'testProperty', null)

        then:
        result == 'testValue'
    }

}
