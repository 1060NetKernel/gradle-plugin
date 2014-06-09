package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 * Simple helper class for handling properties.  Allows for default values to be provided as well as
 * includes internal gradle plugin properties for distribution type properties (version, URLs, etc.)
 *
 * Created by brian on 2/12/14.
 */
class PropertyHelper {

    static final String GRADLE_PLUGIN_PROPERTIES = '/gradle-plugin.properties'

    // Property names (TODO: think about enum?)
    static final String CURRENT_MAJOR_RELEASE_VERSION = 'current_major_release_version'
    static final String DISTRIBUTION_URL_EE = 'distribution_url.ee'
    static final String DISTRIBUTION_URL_SE = 'distribution_url.se'
    static final String DISTRIBUTION_JAR_NAME = 'distribution.jar_name'
    static final String NETKERNEL_INSTANCE = 'netkernel.instance'
    static final String MAVEN_LOCAL_URL = 'maven.local.url'
    static final String MAVEN_NETKERNEL_URL = 'netkernel.maven.url'
    static final String MAVEN_NETKERNELROC_URL = 'netkernelroc.maven.url'

    Properties gradlePluginProperties

    PropertyHelper() {
        gradlePluginProperties = new Properties()
        gradlePluginProperties.load(PropertyHelper.getResourceAsStream(GRADLE_PLUGIN_PROPERTIES))
    }

    /**
     * Find the specified property.  The search looks for the property value in the following order:
     *
     * <ol>
     *     <li>Project property</li>
     *     <li>System property</li>
     *     <li>Internal gradle plugin properties</li>
     *     <li>Default value provided on call</li>
     * </ol>
     *
     * @param project gradle project reference
     * @param propertyName name of property to look for
     * @param defaultValue default value if not found in any other locations
     */
    String findProjectProperty(Project project, String propertyName, String defaultValue = null, Map values = [:]) {
        String retValue = project.hasProperty(propertyName) ? project.getProperties().get(propertyName) : null
        if (retValue == null) {
            retValue = System.properties[propertyName]
            if (retValue == null) {
                retValue = gradlePluginProperties.getProperty(propertyName)
                if (retValue == null) {
                    retValue = defaultValue
                }
            }
        }

        if (values) {
            retValue = retValue?.replaceAll("\\{(.*?)\\}") { globalMatch, name ->
                // Return original match if no value is supplied for the placeholder
                values[name] ?: globalMatch
            }
        }


        retValue
    }

}