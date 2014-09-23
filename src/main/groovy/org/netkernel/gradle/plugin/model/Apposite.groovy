package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 *  A simple class to manage Apposite package configuration.
 */
class Apposite {
    //final AppositeConfig appositeConfig = new AppositeConfig()
    def packageList=[];

    final Project project

    Apposite(Project project) {
        this.project = project
    }

    /*
    def appositeConfig(Closure closure) {
        println("RECEIVED CLOSURE INSIDE APPOSITECONFIG")
        project.configure(appositeConfig, closure)
    }
    */
}
