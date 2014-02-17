package org.netkernel.gradle.plugin.tasks

import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.nk.ExecutionConfig
import org.netkernel.gradle.util.FileSystemHelper

/**
 *
 */
class CleanAllTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def ExecutionConfig executionConfig

    @TaskAction
    def cleanAll() {
    	def nkdir = fsHelper.dirInGradleHomeDirectory("netkernel")
        println("Deleting ${nkdir}")
    	def f=new File(nkdir)
    	f.deleteDir()
    }
}
