package org.netkernel.gradle.plugin.util

import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.netkernel.gradle.plugin.nk.ExecutionConfig

/**
 * A helper class for interacting with a NetKernel instance.
 */
class NetKernelHelper {
    static String BEF = 'http://localhost:1060'

    def FileSystemHelper fileSystemHelper = new FileSystemHelper()

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
        } catch (Throwable t) {
            //TODO: How to log/handle?
        }
        retValue
    }

    def isNetKernelRunning() {
        def result = issueRequest(BEF, Method.GET, [path: '/'])
        result
    }

    /**
     * Start the NetKernel instance described by the specified ExecutionConfig.
     *
     * @param executionConfig
     * @see ExecutionConfig
     */
    def startNetKernel(ExecutionConfig executionConfig) {
        if (!isNetKernelRunning()) {
            def jvm = org.gradle.internal.jvm.Jvm.current()
            def javaBinary = jvm.javaExecutable.absolutePath

            def process
            def workingDir

            switch (executionConfig.mode) {
                case ExecutionConfig.Mode.NETKERNEL_INSTALL:
                    workingDir = fileSystemHelper.dirInGradleHomeDirectory("netkernel/download")
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
            def proc = process.redirectErrorStream(true)
                    .directory(new File(workingDir))
                    .start()
            /* Debug when process couldn't be found - this feeds forked process stdout into Gradle stdout
            def procis=proc.getInputStream()
            Utils.pipe(procis, System.out)
            println("Exit Value: ${proc.exitValue()}")
            */
        } else {
            println "Not starting NetKernel because it is already running on that port"
            // TODO: Throw exception? Ignore?
        }
    }

    /**
     * Returns a NetKernel property value by sending a remote script query to the NetKernel sandbox.
     *
     * NB: This will be replaced with a call to a  proper REST interface after we finish working on all the
     * interactions with NetKernel.
     *
     * @param propertyReference
     * @throws Exception
     */
    def queryNetKernelProperty(String propertyReference) throws Exception {
        def String encodedProperty = java.net.URLEncoder.encode(propertyReference)
        def queryURL = 'http://localhost:1060/tools/scriptplaypen?action2=execute&type=gy&example&identifier&name&space&script=def%20response%20%3D%20context.source(%22' + encodedProperty + '%22)%0Aif%20(null%3D%3Dresponse%20)%20%7B%0A%20%20context.createResponseFrom(%22%22)%0A%20%20%7D%20else%20%7B%0A%20%20context.createResponseFrom(response)%0A%20%20%7D%0A%0A'
        def String queryResponse = new URL(queryURL).text
        return queryResponse
    }

    def whereIsNetKernelInstalled() throws Exception {
        def installLocation = queryNetKernelProperty('netkernel:/config/netkernel.install.path').substring(5)
        return installLocation
    }

    def isNetKernelInstalled() {
        def retValue = false
        def instanceFile = "${fileSystemHelper.gradleHomeDir()}/netkernel/instances"

        if (fileSystemHelper.fileExists(instanceFile)) {
            retValue = (fileSystemHelper.readFileContents(instanceFile).size() >= 1)
        }

        retValue
    }

    def setNetKernelModulesExtensionDirectory() {
        String netkernelInstallDir = whereIsNetKernelInstalled()
        String netkernelProperties = netkernelInstallDir + '/etc/kernel.properties'
        String modulesExtensionProperty = 'netkernel.init.modulesdir'

        def props = new Properties()
        new File(netkernelProperties).withInputStream { stream -> props.load(stream) }
        if (null == props[modulesExtensionProperty]) {
            println 'Adding modules.d support to NetKernel'
            String properties = new File(netkernelProperties).text
            properties = properties + modulesExtensionProperty + "=etc/modules.d\n"
            new File(netkernelProperties).withWriter { writer -> writer.append(properties) }
            // We need to create that directory
            def extDir = new File(netkernelInstallDir + '/etc/modules.d')
            if (!extDir.exists()) {
                extDir.mkdir()
            }
        } else {
            if ('etc/modules.d'.equals(props[modulesExtensionProperty])) {
                println 'NetKernel has proper support for the modules.d extension'
            } else {
                println 'NetKernel has proper support for the modules.d extension at ' + props[modulesExtensionProperty]
            }
        }

    }

    def whereIsModuleExtensionDirectory() throws Exception {
        def extensionDirectoryRelativeLocation = queryNetKernelProperty('netkernel:/config/netkernel.init.modulesdir')
        return extensionDirectoryRelativeLocation
    }


}
