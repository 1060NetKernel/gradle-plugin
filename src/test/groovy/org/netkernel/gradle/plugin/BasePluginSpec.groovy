package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * BasePluginSpec provides helper methods used by concrete tests.
 */
abstract class BasePluginSpec extends Specification {

    Project _project

    Closure assertTaskDependencyClosure = { Project project, String taskName, String dependencyTaskName ->
        project.tasks.findByName(taskName).dependsOn.find { it.toString() == dependencyTaskName }
    }

    File file(String path) {
        return new File(BasePluginSpec.getResource(path).file)
    }

    Project getProject() {
        if (!_project) {
            _project = ProjectBuilder.builder().build()
        }
        _project
    }

    Task createTask(Class clazz) {
        return project.tasks.create(name: clazz.name, type: clazz)
    }

    URL getResource(String name) {
        return BasePluginSpec.getResource(name)
    }

    File getResourceAsFile(String name) {
        return new File(getResource(name).file)
    }

}
