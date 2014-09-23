package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 *  A simple class to manage Apposite package configuration.
 */
class Apposite {
    def packageList=[];

    final Project project

    Apposite(Project project) {
        this.project = project
    }

}
