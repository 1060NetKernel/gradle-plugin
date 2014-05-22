package org.netkernel.gradle.plugin.tasks

import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction

class ModuleResourcesTask extends Copy {

    @TaskAction
    void processModuleResources() {

        project.configurations.getByName('compile').dependencies.each { Dependency dependency ->
            project.configurations.getByName('compile').fileCollection(dependency).each { File file ->
                println "${dependency} ${file}"
                project.copy {
                    from file
                    into "${getRootSpec().getDestinationDir()}/lib"
                }
            }
        }
    }

}
