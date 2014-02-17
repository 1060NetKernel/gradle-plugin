package org.netkernel.gradle.util

import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.netkernel.gradle.nk.ExecutionConfig
import org.netkernel.layer0.util.Utils

/**
 * A helper class for interacting with a NetKernel instance.
 */
class NetKernelHelper {
    static String BEF = 'http://localhost:1060'

    def FileSystemHelper fsHelper = new FileSystemHelper()

    /**
     * Issue the specified request to a running NetKernel instance via
     * HTTP.
     *
     * @param url
     * @param method
     * @param argMap
     * @return
     */
    def issueRequest(String url, Method method, def argMap) {
        def client = new RESTClient(url)
        def retValue = false
        def resp

        try {
            resp = client."${method.toString().toLowerCase()}"(argMap)
            retValue = resp.status == 200
        } catch(Throwable t) {
            //TODO: How to log/handle?
        }
        retValue
    }

    def isNetKernelRunning() {
       def result = issueRequest(BEF, Method.GET, [path : '/'])
       result
    }

    /**
     * Start the NetKernel instance described by the specified ExecutionConfig.
     *
     * @param executionConfig
     * @see ExecutionConfig
     */
    def startNetKernel(ExecutionConfig executionConfig) {
        if(!isNetKernelRunning()) {
            def jvm = org.gradle.internal.jvm.Jvm.current()
            def javaBinary = jvm.javaExecutable.absolutePath

            def process
            def workingDir

            switch(executionConfig.mode) {
                case ExecutionConfig.Mode.NETKERNEL_INSTALL:
                    workingDir = fsHelper.dirInGradleHomeDirectory("netkernel/download")
                    def downloadFile = "${workingDir}/${executionConfig.installJar}"
                    println("Process to be Executed: ${javaBinary} -jar ${downloadFile}")
                    process = new ProcessBuilder(javaBinary, "-jar", downloadFile)
                    break;
                case ExecutionConfig.Mode.NETKERNEL_FULL:
                    workingDir = executionConfig.directory
                    // TODO: Add Windows Handling
                    process = new ProcessBuilder("${workingDir}/bin/netkernel.sh")
                    break;
            }
            def proc=process.redirectErrorStream(true)
                 .directory(new File(workingDir))
                 .start()
            /* Debug when process couldn't be found - this feeds forked process stdout into gradle stdout
            def procis=proc.getInputStream()
            Utils.pipe(procis, System.out)
            println("Exit Value: ${proc.exitValue()}")
            */
        } else {
            println "Not starting NetKernel because it is already running on that port"
            // TODO: Throw exception? Ignore?
        }
    }
}
