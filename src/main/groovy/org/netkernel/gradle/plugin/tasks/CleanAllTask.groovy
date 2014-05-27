package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.util.FileSystemHelper

class CleanAllTask extends DefaultTask {
    FileSystemHelper fsHelper = new FileSystemHelper()
//    def ExecutionConfig executionConfig

    @TaskAction
    def cleanAll() {
        def nkdir = fsHelper.dirInGradleHomeDirectory("netkernel")
        println("Deleting ${nkdir}")
        def f = new File(nkdir)
        f.deleteDir()
    }
}
