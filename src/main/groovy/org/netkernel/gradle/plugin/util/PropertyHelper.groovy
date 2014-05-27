package org.netkernel.gradle.plugin.util

import org.gradle.api.Project

/**
 * Created by brian on 2/12/14.
 */
class PropertyHelper {
    /**
     * Find the specified project property.
     *
     * Priority is:
     * @param p
     * @param propertyName
     */
    def findProjectProperty(Project p, String propertyName, String defaultValue) {
        def retValue = p.hasProperty(propertyName) ? p.getProperties().get(propertyName) : null

        if(retValue == null) {
            retValue = System.properties[propertyName]

            if(retValue == null) {
                retValue = defaultValue
            }
        }

        retValue
    }
}
