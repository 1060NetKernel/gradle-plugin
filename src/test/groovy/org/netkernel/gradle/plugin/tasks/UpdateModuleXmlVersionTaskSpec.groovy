package org.netkernel.gradle.plugin.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.util.ModuleHelper
import spock.lang.Specification

class UpdateModuleXmlVersionTaskSpec extends Specification {

    Project project
    UpdateModuleXmlVersionTask updateModuleXmlVersionTask

    void setup() {
        project = ProjectBuilder.builder().build()
        project.buildDir.mkdirs()

        File sourceModuleXml = new File(UpdateModuleXmlVersionTaskSpec.getResource('/test/sample-module.xml').file)
        ModuleHelper moduleHelper = new ModuleHelper(sourceModuleXml.absolutePath)

        project.ext.nkModuleIdentity = moduleHelper.name

        File outputDir = new File("${project.buildDir}/${project.ext.nkModuleIdentity}")
        outputDir.mkdirs()

        File outputModuleXml = new File(outputDir, "module.xml")

        updateModuleXmlVersionTask = project.tasks.create(name: 'updateModuleXml', type: UpdateModuleXmlVersionTask)
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
