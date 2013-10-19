package org.netkernel.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    void apply(Project project) {
        println project.name
    }
}
