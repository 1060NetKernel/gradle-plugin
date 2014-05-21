package org.netkernel.gradle.util

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PropertyHelperSpec extends Specification {

    PropertyHelper propertyHelper
    Project project

    void setup() {
        propertyHelper = new PropertyHelper()

        project = ProjectBuilder.builder().withName('propertyHelperTest').build()
    }

    def 'gets property value from project'() {
        when:
        String value = propertyHelper.findProjectProperty(project, 'name', 'default')

        then:
        value == 'propertyHelperTest'

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

}
