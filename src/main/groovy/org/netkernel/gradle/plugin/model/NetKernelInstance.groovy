package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.gradle.api.Project
import org.apache.tools.ant.taskdefs.condition.Os

/**
 * A NetKernelInstance represents an individual instance of NetKernel.  Both the downloaded
 * jar file and installed directory are represented by a single instance to simplify the use.
 * Methods are provided to start and stop the instance as well as deploy/undeploy modules.
 */
@Slf4j
// Used to keep map constructor from GroovyObject
@InheritConstructors
class NetKernelInstance implements Serializable {

    String name
    URL url
    int backendPort
    int frontendPort

    // Location is the installed directory of NetKernel
    File location

    // Location of downloaded distribution jar file
    File jarFileLocation

    // Location of frozen jar file for freezing/thawing
    File frozenJarFile

    // The frozen location is where the NetKernel instance is copied to prior to jar file creation
    File frozenLocation

    //Maps for freeze/thaw configuration
    def freezeConfig
    Map thawConfig

    // Standard or Enterprise
    Edition edition

    // NetKernel Version
    String netKernelVersion

    Project project

    /**
     * Constructor used by gradle for calls to project.container(NetKernelInstance).  Otherwise,
     * the inherited Map constructor is used.
     *
     * @param name name of instance
     */
    NetKernelInstance(String name) {
        this.name = name
    }

    //Declare maven dependency to thaw
    def thaw (dependencyMap)
    {   thawConfig = dependencyMap

    }

    def eggMeetChicken()
    {   if(thawConfig!=null) {
            project.configurations.create("thawrepo")
            project.dependencies.thawrepo thawConfig
        }
    }

    //Declare freeze config using dependency syntax for symmetry
    def freeze (freezeMap)
    {   freezeConfig = freezeMap
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
     * Determines if an instance's Apposite packages are up to date.
     *
     * @return true if the instance is uptodate: false otherwise.
     */
    boolean isUpToDate() {
        if(edition.equals(Edition.ENTERPRISE)) {
            boolean result = false
            try {
                HttpResponse response = issueRequest(Method.GET, [path: '/tools/apposite/unattended/v1/installed'])
                def resultset = response.getData()
                def updates=resultset.row.findAll { it.HASUPDATE == 'true' }.size()
                println("APPOSITE UPDATES AVAILABLE: " + updates)
                result = response.statusLine.statusCode == HttpStatus.SC_OK && updates==0
            } catch (Exception e) {
                throw e;
            }
            return result
        }
        else
        {   throw new Exception ("Unattended Apposite is not available on SE")
        }
    }

    /**
     * Starts NetKernel from installed directory
     */
    void start() {
        def startscript
        def loc=location.absolutePath
        if(isWindows())
        {   startscript="netkernel.bat"
            if(loc.contains(" ")) {
                loc = "\"${loc}\""
            }
        }
        else
        {   startscript="netkernel.sh"
        }
        doStart(location, "${loc}/bin/${startscript}")
    }

    /**
     * Starts NetKernel from jarFileLocation
     */
    void startJar() {
        def jvm = org.gradle.internal.jvm.Jvm.current()
        def javaBinary = jvm.javaExecutable.absolutePath
        //println "Starting NK jar:  ${jarFileLocation.absolutePath} in directory ${location.parentFile}"
        location.parentFile.mkdirs()
        if(!jarFileLocation.exists())
        {   throw new Exception("${jarFileLocation} does not exist - can't start the NK from jar")
        }
        def jarfile=jarFileLocation.absolutePath
        if(isWindows())
        {   //jarfile=jarfile.replaceAll(" ", "%20")  //Take care of stupid windows username folders
            if(jarfile.contains(" "))
            {   jarfile="\"${jarfile}\""
            }
            if(javaBinary.contains(" "))
            {   javaBinary="\"${javaBinary}\""
            }
        }
        doStart(location.parentFile, javaBinary, '-jar', "${jarfile}")
    }

    /**
     * Actually start the NetKernel instance by creating a {@link Process}
     */
    void doStart(File workingDir, String... command) {
        if (!isRunning()) {
            println("Process to be Executed: ${command}")
            ProcessBuilder processBuilder = new ProcessBuilder(command)
            Process process = processBuilder.redirectErrorStream(true).directory(workingDir).start()

            /* Debug when process couldn't be found - this feeds forked process stdout into Gradle stdout
            def procis=proc.getInputStream()
            Utils.pipe(procis, System.out)
            println("Exit Value: ${proc.exitValue()}")
            */
        } else {
            log.warn "Not starting NetKernel because it is already running"
            throw new Exception("Not starting NetKernel because it is already running")
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
     * Installs NetKernelInstance to installation directory if not done already.
     *
     * @throws IllegalStateException if already installed
     */
    void install() {

        startJar()

        while (!running) {
            log.info "Waiting for NetKernel to start..."
            Thread.sleep(500)
            //TODO: Timeout eventually
        }

        try {
            if (issueRequest(Method.POST, [
                    path : '/installer/',
                    query: [target           : location,
                            expand           : 'yes',
                            proxyHost        : '',
                            proxyPort        : '',
                            username         : '',
                            password         : '',
                            ntWorkstationHost: '',
                            ntDomain         : '']])) {
                log.info "Successfully installed NetKernel in ${location}"
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
                    log.info "Error installing NetKernel to ${location}"
                }
            } else {
                log.info "Installation didn't go as planned..."
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t)
        }
    }

    /**
     * Verifies that instance has the appropriate setup for using the modules.d folder.  This
     * includes creating the etc/modules.d folder as well as adding the appropriate property
     * to the etc/kernel.properties file.
     */
    void initializeModulesDir() {
        File kernelPropertiesFile = new File(location, 'etc/kernel.properties')
        File modulesDDirectory = new File(location, 'etc/modules.d')
        String modulesExtensionProperty = 'netkernel.init.modulesdir'

        Properties properties = new Properties()
        kernelPropertiesFile.withReader { reader ->
            properties.load(reader)
        }
        if (!properties[modulesExtensionProperty]) {
            log.debug "Adding modules.d support to NetKernel instance ${this}"
            kernelPropertiesFile.append """
                # Directory to support daemon modules
                ${modulesExtensionProperty}=etc/modules.d
            """.stripIndent()
        }
        modulesDDirectory.mkdirs()
    }

    /**
     * Adds a new file to the modules.d folder using the name of the archive file.  So if the
     * archive file is module-1.0.0.jar, a file named module-1.0.0.jar.xml is created in the modules.d
     * folder with a reference to the archive file.
     *
     * @param moduleArchiveFile module file to deploy
     */
    void deploy(File moduleArchiveFile) {
        log.debug "Deploying ${moduleArchiveFile} to ${this}"
        File moduleReference = new File(location, "etc/modules.d/${moduleArchiveFile.name}.xml")
        moduleReference.text = """
        <modules>
        <module runlevel="7">${moduleArchiveFile.absolutePath}</module>
        </modules>
        """.stripIndent()
    }

    /**
     * Undeploy simply removes the file from the modules.d folder that references the archive file.  The
     * method assumes that the name of the file will be {archive file name}.xml.
     *
     * @param moduleArchiveFile module file to undeploy
     */
    void undeploy(File moduleArchiveFile) {
        log.debug "Undeploying ${moduleArchiveFile} from ${this}"
        new File(location, "etc/modules.d/${moduleArchiveFile.name}.xml").delete()
    }

    boolean runXUnit()
    {   boolean result = false
        try {
            println("Running Xunit Tests.  Please wait...")
            HttpResponse response = issueRequest(Method.GET, [path: '/test/exec/xml/all'])

            def testResults=response.getData()
            //XmlSlurper testResults = new XmlSlurper().parse(is)
            def results=testResults.totalResults

            println("Total Tests: "+results.testTotal)
            println("Success: "+results.testSuccess)
            println("Failures: "+results.testFailException)
            println("Failed Assertions: "+results.testFailAssert)
            println("\nExecution Time: ${results.testExecutionTime}  Total Time: ${results.testTotalTime}\n")

            result = response.statusLine.statusCode == HttpStatus.SC_OK && results.testTotal.equals(results.testSuccess)
            if(result)
            {   println("===========================\nXUNIT TESTS PASSING\n===========================")
            }
            else
            {   println("!!!!!!!!!!!!!!!!!!!!!!!!!!!\nXUNIT TESTS FAILING\n!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            }
        } catch (Exception e) {
            throw e
        }
        return result
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
     * Used to allow strings to be used for locations in the build.gradle file.
     *
     * @param location absolute path of location
     */
    void setLocation(String location) {
        this.location = new File(location)
    }

    /**
     * Sets the edition.  Used primarily for instances added from build.gradle file.
     *
     * @param e Edition as a String 'EE' or 'SE'
     */
    void setEdition(String e) {
        this.edition = Edition.values().find { it.name == e }
    }

    /**
     * Sets the version.  This is to make it easier within the build.gradle files.
     *
     * @param v version number as string
     */
    void setVersion(String v) {
        this.netKernelVersion = v
    }

    /**
     * Sets the URL as a string used primarily by build.gradle file.
     *
     * @param url string netKernelVersion of url
     */
    void setUrl(String url) {
        this.url = new URL(url)
    }

    String toString() {
        return "NetKernel Instance: ${name}\nLocation: ${location}\nEdition: ${this.edition} ${this.netKernelVersion}\nAdmin Fulcrum: ${this.url}:${this.backendPort}"
    }

    /*********
     * FREEZE Parameters
     */
    String getFreezeGroup()
    {   def s
        if(freezeConfig!=null) {
            s = freezeConfig["group"]
        }
        if(s==null)
        {   s=project.group
        }
        if(s==null)
        {   s="org.netkernel"
        }
        return s
    }

    String getFreezeName()
    {   def s
        if(freezeConfig!=null) {
            s = freezeConfig["name"]
        }
        if(s==null)
        {   s=project.name
        }
        return s
    }
    String getFreezeVersion()
    {   def s
        if(freezeConfig!=null) {
            s = freezeConfig["version"]
        }
        if(s==null)
        {   s=project.version
        }
        if(s==null)
        {   s="1.1.1"
        }
        return s
    }

    File getFreezeLocation()
    {   def f
        if(frozenLocation!=null)
        {   f=frozenLocation
        }
        else {
            //Freeze to a directory next to the installation
            f=new File(location.parentFile, location.name+"-freeze/");
        }
        return f
    }

    File getFrozenJarFile()
    {   def f
        if(frozenJarFile!=null)
        {   f=frozenJarFile
        }
        else {
            f=new File(getFreezeLocation(), getFreezeName()+".jar");
        }
        return f
    }

    boolean exists()
    {   def f=new File(location, "bin/")
        return f.exists()
    }

    boolean isWindows()
    {   return Os.isFamily(Os.FAMILY_WINDOWS)
    }
}
