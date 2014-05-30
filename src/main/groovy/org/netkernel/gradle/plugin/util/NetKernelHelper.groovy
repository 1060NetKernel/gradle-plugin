package org.netkernel.gradle.plugin.util

import groovy.util.logging.Slf4j
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.netkernel.gradle.plugin.nk.ExecutionConfig

/**
 * A helper class for interacting with a NetKernel instance.
 */
@Slf4j
class NetKernelHelper {
    static URL BEF = new URL('http://localhost:1060')

    FileSystemHelper fileSystemHelper = new FileSystemHelper()

    /**
     * Issue the specified request to a running NetKernel instance via HTTP.  The arguments supplied
     * are passed onto the {@link groovyx.net.http.RESTClient}.
     *
     * @param url typically the backend fulcrum URL, but can be any URL
     * @param method http method (get, post, etc.)
     * @param argMap arguments passed to RESTClient
     *
     * @return true if request returns 200; false otherwise
     */
    boolean issueRequest(URL url, Method method, Map argMap) {
        boolean retValue = false

        try {
            HttpResponse response = new RESTClient(url)."${method.toString().toLowerCase()}"(argMap)
            retValue = response.statusLine.statusCode == 200
        } catch (Throwable t) {
            //TODO: How to log/handle?
        }

        return retValue
    }

    /**
     * Issues a simple GET request to a NetKernel instance to see if it is running.
     *
     * @return true if NetKernel is running; false otherwise
     */
    // TODO - This should be parameterized to accept ExecutionConfig, or perhaps an instance of this class per ExecutionConfig
    boolean isNetKernelRunning() {
        return issueRequest(BEF, Method.GET, [path: '/'])
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
                .directory(workingDir)
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
     * Stops a NetKernel instance.  The ExecutionConfig provides the URL for the backend port.
     *
     * @param executionConfig details about an instance of NetKernel
     */
    void stopNetKernel(ExecutionConfig executionConfig) {
        if (isNetKernelRunning()) {
            if (issueRequest(executionConfig.backendFulcrum, Method.POST,
                [path: '/tools/shutdown', query: [confirm: '1', action2: 'force']])) {
                while (isNetKernelRunning()) {
                    log.info "Waiting for NetKernel to shutdown..."
                    Thread.sleep(500)
                }
            } else {
                log.error "Error occurred trying to shutdown NetKernel at ${executionConfig.backendFulcrum}"
            }
        } else {
            log.info "NetKernel at ${executionConfig.backendFulcrum} is not running"
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
        File instanceFile = fileSystemHelper.dirInGradleHomeDirectory('netkernel/instances')

        if (instanceFile.exists()) {
            retValue = instanceFile.text.size() >= 1
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
