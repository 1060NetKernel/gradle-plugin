package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.util.ModuleHelper

class UpdateModuleXmlVersionTask extends DefaultTask {

    @InputFile
    File sourceModuleXml

    @OutputFile
    File outputModuleXml

    @TaskAction
    void updateModuleXmlVersion() {
        ModuleHelper sourceModule = new ModuleHelper(sourceModuleXml.absolutePath)

        // Remove '-SNAPSHOT' from version
        String version = project.version.replace('-SNAPSHOT', '')

        outputModuleXml.text = sourceModuleXml.text.replace(
            "<version>${sourceModule.version}</version>", "<version>${version}</version>"
        )
    }

}
