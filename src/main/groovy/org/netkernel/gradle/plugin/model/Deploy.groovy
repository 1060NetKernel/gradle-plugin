package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 *  A simple class to manage Apposite package configuration.
 */
class Deploy {
    def collection
    def modules=[]

    final Project project

    Deploy(Project project) {
        this.project = project
    }

    def module (moduleMap)
    {   modules.add(moduleMap)
    }
}
