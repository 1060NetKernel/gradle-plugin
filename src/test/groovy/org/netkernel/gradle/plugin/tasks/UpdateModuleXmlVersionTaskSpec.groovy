package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.Module

class UpdateModuleXmlVersionTaskSpec extends BasePluginSpec {

    UpdateModuleXmlVersionTask updateModuleXmlVersionTask

    void setup() {
        File sourceModuleXml = file '/test/sample-module.xml'
        Module module = new Module(sourceModuleXml)

        createNetKernelExtension()

        project.extensions.netkernel.module = module

        File outputDir = new File("${project.buildDir}/${module.name}")
        outputDir.mkdirs()

        File outputModuleXml = new File(outputDir, "module.xml")

        updateModuleXmlVersionTask = createTask(UpdateModuleXmlVersionTask)
        updateModuleXmlVersionTask.sourceModuleXml = sourceModuleXml
        updateModuleXmlVersionTask.outputModuleXml = outputModuleXml
    }

    def 'updates module version for placeholder module xml'() {
        setup:
        project.version = version

        when:
        updateModuleXmlVersionTask.updateModuleXmlVersion()

        then:
        new XmlSlurper().parse(updateModuleXmlVersionTask.outputModuleXml).meta.identity.version.text() == expectedVersion

        where:
        version          | expectedVersion
        '1.0.0'          | '1.0.0'
        '1.0.0-SNAPSHOT' | '1.0.0'
    }

}
