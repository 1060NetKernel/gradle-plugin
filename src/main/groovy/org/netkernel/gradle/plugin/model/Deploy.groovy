package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 *  A simple class to manage deploy collection configuration.
 */
class Deploy {
    def collection

    final Project project

    Deploy(Project project) {
        this.project = project
    }

    def module (moduleMap)
    {   project.dependencies.nkdeploy moduleMap
    }
}
