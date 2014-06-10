package org.netkernel.gradle.plugin.model

import org.netkernel.gradle.plugin.BasePluginSpec

class PropertyHelperSpec extends BasePluginSpec {

    PropertyHelper propertyHelper

    void setup() {
        propertyHelper = new PropertyHelper()
    }

    def 'gets property value from project'() {
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

    def 'gets property and populates placeholder variables'() {
        when:
        String result = propertyHelper.findProjectProperty(project, 'testPropertyWithPlaceholder', null, [name1: 'value1', name2: 'value2'])

        then:
        result == 'testValue/value1/value2/{notSubstituted}'
    }

    def 'gets property and uses other property value'() {
        when:
        String result = propertyHelper.findProjectProperty(project, 'testPropertyWithOtherProperty', null)

        then:
        result == "testValue/testValue"
    }

}
