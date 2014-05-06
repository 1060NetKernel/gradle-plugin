package org.netkernel.gradle.plugin

import org.gradle.api.Project
import spock.lang.Specification

class BasePluginSpec extends Specification {

    Closure assertTaskDependencyClosure = { Project project, String taskName, String dependencyTaskName ->
        project.tasks.findByName(taskName).dependsOn.find { it.toString() == dependencyTaskName }
    }

}
