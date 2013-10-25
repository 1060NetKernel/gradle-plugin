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
class InstallNetKernelTask extends DefaultTask {
    def NetKernelHelper nkHelper = new NetKernelHelper()
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def ExecutionConfig executionConfig

    @TaskAction
    def installNK() {
        while(!nkHelper.isNetKernelRunning()) {
            println "Waiting for NetKernel to start..."
            Thread.sleep(500)
            //TODO: Timeout eventually
        }

        //TODO: Check for initialization
        def installationDir = executionConfig.directory

        //TODO: Directory already exists handling?
        if(fsHelper.dirExists(installationDir)||
           fsHelper.createDirectory(installationDir)) {

            // TODO: Move these details to NetKernelHelper and pass in
            // the ExecutionConfig

            if(nkHelper.issueRequest(NetKernelHelper.BEF, Method.POST,
                 [path : '/installer/',
                  query : [ target: installationDir,
                            expand: 'yes',
                            proxyHost: '',
                            proxyPort: '',
                            username: '',
                            password: '',
                            ntWorkstationHost: '',
                            ntDomain: '']]))
            {
                println "Successfully installed NetKernel in ${installationDir}"
                println "Shutting NetKernel down..."

                if(nkHelper.issueRequest(NetKernelHelper.BEF, Method.POST,
                   [path: '/tools/shutdown',
                    query : [confirm : '1', action2 : 'force']])) {

                    while(nkHelper.isNetKernelRunning()) {
                        println "Waiting for NetKernel to shutdown..."
                        Thread.sleep(500)
                    }
                    println "Installation complete."
                } else {
                    println "Error installing NetKernel to ${installationDir}"
                }
            } else {
                println "Installation didn't go as planned..."
            }
        }
    }
}
