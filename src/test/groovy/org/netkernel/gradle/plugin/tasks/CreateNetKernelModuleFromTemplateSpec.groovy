package org.netkernel.gradle.plugin.tasks

import org.gradle.execution.taskgraph.TaskInfo
import spock.lang.Specification

class CreateNetKernelModuleFromTemplateSpec extends Specification {

    CreateModuleFromTemplateTask createNetKernelModuleFromTemplate

    void setup() {
        createNetKernelModuleFromTemplate = new CreateModuleFromTemplateTask(new TaskInfo())
    }






}
