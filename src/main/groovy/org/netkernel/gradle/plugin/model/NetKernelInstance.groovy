package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus

/**
 * A NetKernelInstance represents an indvidual instance of NetKernel.  This can be
 * a distribution jar file downloaded from 1060 or an installed local instance.  Methods
 * are provided to start and stop the instance as well as deploy/undeploy of modules.
 */
@Slf4j
// Used to keep map constructor from GroovyObject
@InheritConstructors
class NetKernelInstance {

    String name
    URL url
    int backendPort
    int frontendPort

    // Location can either be a directory or jar file reference
    File location

    // Location to install instance into.  Only applicable for jar instances
    File installationDirectory

    // Standard or Enterprise
    Release release

    /**
     * Constructor used by gradle for calls to project.container(NetKernelInstance).  Otherwise,
     * the inherited Map constructor is used.
     *
     * @param name name of instance
     */
    NetKernelInstance(String name) {
        this.name = name
    }

    /**
     * Determines if an instance is running.  The request is made to the root of the
     * backend fulcrum and returns true only if a 200 OK status is returned.
     *
     * @return true if the instance is running; false otherwise.
     */
    boolean isRunning() {
        boolean result = false
        try {
            HttpResponse response = issueRequest(Method.GET, [path: '/'])
            result = response.statusLine.statusCode == HttpStatus.SC_OK
        } catch (Exception e) {
            // Any exception will return that NetKernel is not running
        }
        return result
    }

    /**
     * Starts the NetKernel instance by created a {@link Process}
     */
    void start() {
        if (!isRunning()) {
            def jvm = org.gradle.internal.jvm.Jvm.current()
            def javaBinary = jvm.javaExecutable.absolutePath

            ProcessBuilder processBuilder
            File workingDir

            if (location.directory) {
                workingDir = location
                // TODO: Add Windows Handling
                processBuilder = new ProcessBuilder("${location.absolutePath}/bin/netkernel.sh")

            } else {
                workingDir = location.parentFile
                println("Process to be Executed: ${javaBinary} -jar ${location.absolutePath}")
                processBuilder = new ProcessBuilder(javaBinary, "-jar", location.absolutePath)

            }

            Process process = processBuilder.redirectErrorStream(true)
                .directory(workingDir)
                .start()
            /* Debug when process couldn't be found - this feeds forked process stdout into Gradle stdout
            def procis=proc.getInputStream()
            Utils.pipe(procis, System.out)
            println("Exit Value: ${proc.exitValue()}")
            */
        } else {
            log.warn "Not starting NetKernel because it is already running"
            // TODO: Throw exception? Ignore?
        }
    }

    /**
     * Stops the NetKernel instance by issuing a shutdown call to the backend fulcrum.
     */
    void stop() {
        if (isRunning()) {

            HttpResponse response = issueRequest(Method.POST, [
                path : '/tools/shutdown',
                query: [config: '1', action2: 'force']])

            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                while (isRunning()) {
                    log.info "Waiting for NetKernel instance ${this} to shutdown"
                    Thread.sleep(500)
                }
            } else {
                log.error "Error occurred attempting to shut down NetKernel instance (${this})"
            }

        } else {
            log.info "NetKernel instance ${this} is not running"
        }
    }

    /**
     * Gets an instance property using the scriptplaypen in the backend fulcrum.
     *
     * @param propertyName name of property
     *
     * @return value of property if found, empty string otherwise.
     */
    String getInstanceProperty(String propertyName) {

        String groovyScript = """
            def response=context.source("${propertyName}");
            context.createResponseFrom(response ?: "")
            """

        HttpResponse response = issueRequest(Method.GET, [
            path : '/tools/scriptplaypen',
            query: [
                action2   : 'execute',
                type      : 'gy',
                example   : "",
                identifier: "",
                name      : "",
                space     : "",
                script    : groovyScript]])

        if (response.statusLine.statusCode == HttpStatus.SC_OK) {
            return response.entity.content.text
        } else {
            return ''
        }
    }

    /**
     * Installs NetKernelInstance to installation directory. This only applies for jar
     * instances and during initial plugin bootstrapping, install tasks should only be
     * created for jar types.
     *
     * @throws IllegalStateException if called on directory instance
     */
    void install() {
        if (!canInstall()) {
            throw new IllegalStateException("${this} is not a jar file.  Cannot install.")
        }

        start()

        while (!running) {
            log.info "Waiting for NetKernel to start..."
            Thread.sleep(500)
            //TODO: Timeout eventually
        }

        try {
            //TODO: Directory already exists handling?
            if (installationDirectory.exists() || installationDirectory.mkdirs()) {

                if (issueRequest(Method.POST, [
                    path : '/installer/',
                    query: [target           : installationDirectory,
                            expand           : 'yes',
                            proxyHost        : '',
                            proxyPort        : '',
                            username         : '',
                            password         : '',
                            ntWorkstationHost: '',
                            ntDomain         : '']])) {
                    log.info "Successfully installed NetKernel in ${installationDirectory}"
                    log.info "Shutting NetKernel down..."

                    if (issueRequest(Method.POST, [
                        path : '/tools/shutdown',
                        query: [confirm: '1', action2: 'force']])) {

                        while (isRunning()) {
                            log.info "Waiting for NetKernel to shutdown..."
                            Thread.sleep(500)
                        }
                        log.info "Installation complete."
                    } else {
                        log.info "Error installing NetKernel to ${installationDirectory}"
                    }
                } else {
                    log.info "Installation didn't go as planned..."
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t)
        }
    }

    void deploy(Module module) {

    }

    void undeploy(Module module) {

    }

    /**
     * Issues request to backend fulcrum.
     *
     * @param method http method (GET, POST, etc.)
     * @param args Map of arguments passed to rest client
     *
     * @return {@link HttpResponse} from invoking request
     *
     * @throws URISyntaxException
     * @throws org.apache.http.client.ClientProtocolException
     * @throws IOException
     */
    HttpResponse issueRequest(Method method, Map args) {
        return new RESTClient("${url}:${backendPort}")."${method.toString().toLowerCase()}"(args)
    }

    /**
     * Determines if the NetKernel instance can be installed.  This is specific to jar instances
     * that have an installationDirectory specified.
     *
     * @return true if can install; false otherwise
     */
    boolean canInstall() {
        return !location.directory && installationDirectory
    }

    /**
     * Determines if modules can be deployed. This is specific to instances that are directories
     * and does not apply to jar files.
     *
     * @return true if module can be deployed; false otherwise
     */
    boolean canDeployModule() {
        return location.directory
    }

    String toString() {
        return name
    }


//    def setNetKernelModulesExtensionDirectory() {
//        String netkernelInstallDir = whereIsNetKernelInstalled()
//        String netkernelProperties = netkernelInstallDir + '/etc/kernel.properties'
//        String modulesExtensionProperty = 'netkernel.init.modulesdir'
//
//        def props = new Properties()
//        new File(netkernelProperties).withInputStream { stream -> props.load(stream) }
//        if (null == props[modulesExtensionProperty]) {
//            println 'Adding modules.d support to NetKernel'
//            String properties = new File(netkernelProperties).text
//            properties = properties + modulesExtensionProperty + "=etc/modules.d\n"
//            new File(netkernelProperties).withWriter { writer -> writer.append(properties) }
//            // We need to create that directory
//            def extDir = new File(netkernelInstallDir + '/etc/modules.d')
//            if (!extDir.exists()) {
//                extDir.mkdir()
//            }
//        } else {
//            if ('etc/modules.d'.equals(props[modulesExtensionProperty])) {
//                println 'NetKernel has proper support for the modules.d extension'
//            } else {
//                println 'NetKernel has proper support for the modules.d extension at ' + props[modulesExtensionProperty]
//            }
//        }
//
//    }
//
//    def whereIsModuleExtensionDirectory() throws Exception {
//        def extensionDirectoryRelativeLocation = queryNetKernelProperty('netkernel:/config/netkernel.init.modulesdir')
//        return extensionDirectoryRelativeLocation
//    }
}
