package org.netkernel.gradle.plugin.tasks

import groovy.xml.MarkupBuilder

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import org.netkernel.gradle.plugin.ExecutionConfig
import org.netkernel.gradle.util.FileSystemHelper

class DeployDaemonModuleTask extends DefaultTask {
    def ExecutionConfig executionConfig
    def FileSystemHelper fsHelper = new FileSystemHelper()
    
    @TaskAction
    def deployModule() {
        def moduleName = project.name
        
        def sw = new StringWriter()
        def mb = new MarkupBuilder(sw)
        mb.setDoubleQuotes(true)
        
        mb.modules {
            module(runlevel : "7", type : "deploy", "${project.projectDir}/build/${project.name}/")
        }
        
        def file = new File("${executionConfig.directory}/etc/modules.d/${moduleName}.xml")
        file.write(sw.toString())
    }
}