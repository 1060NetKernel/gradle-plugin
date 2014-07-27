package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.Module

class UpdateModuleXmlVersionTask extends DefaultTask {

    @InputFile
    File sourceModuleXml

    @OutputFile
    File outputModuleXml

    @TaskAction
    void updateModuleXmlVersion() {
        Module sourceModule = new Module(sourceModuleXml)

        // Remove '-SNAPSHOT' from netKernelVersion
        String version = project.version.replace('-SNAPSHOT', '')

        outputModuleXml.text = sourceModuleXml.text.replace(
            "<version>${sourceModule.version}</version>", "<version>${version}</version>"
        )
    }

}
