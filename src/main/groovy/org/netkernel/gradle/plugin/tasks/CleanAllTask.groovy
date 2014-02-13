package org.netkernel.gradle.plugin.tasks

import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.ExecutionConfig
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.NetKernelHelper

/**
 *
 */
class CleanAllTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def ExecutionConfig executionConfig

    @TaskAction
    def cleanAll() {
    	println("Deleting ~/.gradle/netkernel/")
    	def nkdir = fsHelper.dirInGradleHomeDirectory("netkernel")
    	def f=new File(nkdir)
    	f.deleteDir()
    }
}
