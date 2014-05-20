package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class BasePluginSpec extends Specification {

    Project _project

    Closure assertTaskDependencyClosure = { Project project, String taskName, String dependencyTaskName ->
        project.tasks.findByName(taskName).dependsOn.find { it.toString() == dependencyTaskName }
    }

    File file(String path) {
        return new File(BasePluginSpec.getResource(path).file)
    }

    Project getProject() {
        if(!_project) {
            _project = ProjectBuilder.builder().build()
        }
        _project
    }

}
