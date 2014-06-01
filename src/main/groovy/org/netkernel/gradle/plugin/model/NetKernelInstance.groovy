package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus

@Slf4j
@InheritConstructors // Used to keep map constructor from GroovyObject
class NetKernelInstance {

    String name
    URL url
    int backendPort
    int frontendPort

    // Location can either be a directory or jar file reference
    File location

    Edition edition

    NetKernelInstance(String name) {
        this.name = name
    }

    boolean isRunning() {
        HttpResponse response = issueRequest(Method.GET, [path: '/'])
        return response.statusLine.statusCode == HttpStatus.SC_OK
    }

    boolean start() {
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
            return ""
        }
    }

    void deploy(Module module) {

    }

    void undeploy(Module module) {

    }

    String toString() {
        return "${url}"
    }

    private HttpResponse issueRequest(Method method, Map args) {
        return new RESTClient(url)."${method.toString().toLowerCase()}"(args)
    }

}
