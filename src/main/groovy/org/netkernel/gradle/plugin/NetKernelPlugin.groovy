package org.netkernel.gradle.plugin

import org.gradle.api.*
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
//import org.gradle.api.tasks.Upload
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.impldep.org.apache.maven.project.ProjectUtils;
import org.netkernel.gradle.plugin.model.*
import org.netkernel.gradle.plugin.tasks.*
import org.gradle.util.GradleVersion
import groovy.util.logging.Slf4j

import static org.netkernel.gradle.plugin.model.PropertyHelper.*
import static org.netkernel.gradle.plugin.tasks.TaskName.*

//Needed for update to maven-publish
import org.gradle.api.publish.maven.MavenPublication

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
@Slf4j
class NetKernelPlugin implements Plugin<Project> {

    Project project
    def ANDROID_DX_SWITCHES=""

    // The primary model class for the build.
    NetKernelExtension netKernel

    void apply(Project project) {
        this.project = project
        project.apply plugin: 'java'        
        project.apply plugin: 'maven-publish'

        println("Gradle NetKernel Plugin v2.3.1")
        configureProject()
        createTasks()
        configureTasks()
        createTaskDependencies()
        afterEvaluate()
    }

    /**
     * Initial configuration for the plugin.  This is where the backing model is initialized
     * and configurations are added.  Also determined is the source structure of the project,
     * be it NetKernel or gradle.
     */
    void configureProject() {

        ['freeze', 'thaw', 'provided','nkdeploy'].each { name ->
            project.configurations.create(name)
        }

        // This is so that provided dependencies are on the classpath, but are filtered out during the build
        project.configurations.compileClasspath.extendsFrom(project.configurations.provided)

        netKernel = project.extensions.create("netkernel", NetKernelExtension, project)
        netKernel.instances = createNetKernelInstances()

        // Determine project structure
        if (new File(project.projectDir, "src/module.xml").exists()) {
            netKernel.sourceStructure = SourceStructure.NETKERNEL
        } else {
            netKernel.sourceStructure = SourceStructure.GRADLE
        }

        def libDir

        switch (netKernel.sourceStructure) {
            case SourceStructure.NETKERNEL:
                def baseDir = new File(project.projectDir, 'src/')
                //Configure the javaCompiler
                def fileTree = project.fileTree(dir: baseDir, includes: ['**/*.java'])
                //fileTree.visit { f ->  println f }
                project.tasks.compileJava.configure {
                    source = fileTree
                }

                netKernel.module = new Module(project.file('src/module.xml'))

                libDir = new File(baseDir, "lib/")

                break;
            case SourceStructure.GRADLE:
                if (project.file('src/module/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/module/module.xml'))
                    libDir = new File(project.projectDir, "src/module/lib/")
                } else if (project.file('src/main/resources/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/main/resources/module.xml'))
                    libDir = new File(project.projectDir, "src/main/resources/lib/")
                }

                break;
        }
        //Deal with any libraries in the module
        if (libDir!=null && libDir.exists()) {
            def libTree = project.fileTree(dir: libDir, includes: ['**/*.jar'])
            libTree.visit { f ->
                //println "lib/ DEPENDENCY ADDED: ${f}"
            }
            project.dependencies.add("implementation", libTree)
        }


        if (!netKernel.module) {
            //throw new InvalidUserDataException("Could not find module.xml in the project.")
            System.err.println("NetKernel Plugin: Could not find module.xml in the project - only sysadmin tasks will be available")
            /* Doesn't seem to do anything!
            ['build', 'assemble', 'jar', 'classes', 'buildDependents', 'buildNeeded', 'clean'].each{ task ->
                project.tasks[task].enabled false
            }
            */
        }

        // If the project has a netKernelVersion specified, override the value in the module.xml
        if(netKernel.module) {
            if (project.version == 'unspecified') {
                project.version = netKernel.module.version
            } else {
                netKernel.module.version = project.version
            }
            //Set Maven Artifact name and netKernelVersion
            //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
            project.archivesBaseName = netKernel.module.URIDotted

            println "MODULE TARGET ${netKernel.module.name}"

            println("Finished configuring srcStructure")

        }
    }

    /**
     * Creates all of the tasks for this plugin.  Some tasks may be disabled depending on
     * what is provided by the build.gradle file.
     */
    void createTasks() {

        String groupName = "NetKernel Helper"
        
        createTask(DOWNLOAD, DownloadNetKernelTask, 'Downloads NetKernel EE or SE edition', groupName)

        if(netKernel.module) {

            createTask(MODULE, Copy, "Copies built classes to build folder", groupName)

            createTask(MODULE_RESOURCES, Copy, 'Copies module resources (module.xml, etc.) to build folder and resolves lib/ dependencies', groupName)

            createTask(UPDATE_MODULE_XML_VERSION, UpdateModuleXmlVersionTask, 'Updates version in module xml to match project version', groupName)
                    .setEnabled(netKernel.module.versionOverridden)
                    
            //Android DEX tasks
            createTask(DEX, DefaultTask, "Build Java module and cross compile to Android DEX bytecode (requires 'dx' on the path)", "Android")
            createTask(DEXLIB, DefaultTask, "", null)
            createTask(DEXUNPACK, DefaultTask, "", null)
            //Get dex min version or null
            try
            {
            	ANDROID_DX_SWITCHES=project.rootProject.gradle.ext.ANDROID_DX_SWITCHES
            }
            catch(e)
            {	//Ignore as no switches provided            	
            }
        }
    }

    /**
     * Any custom configuration for the tasks is done here.  Specific data needed by the tasks
     * is provided by the backing model classes (e.g. {@see NetKernelExtension})
     */
    void configureTasks() {

        configureTask(DOWNLOAD) {
            doFirst()
                {
                    download = netKernel.download
                    netKernelVersion = netKernel.currentMajorReleaseVersion()
                    destinationFile = netKernel.workFile("download/${netKernel.distributionJarFile(download.edition, netKernelVersion)}")
                    println("DOWNLOAD DESTINATION ${destinationFile}")
                    destinationFile.getParentFile().mkdirs()
                }
        }

        if(netKernel.module) {
        	
        	createTask(NKECLIPSE, NetKernelEclipse, "Runs eclipse target then makes eclipse/gradle point to same versioned build/ location for dynamic module support", "IDE")
        	configureTask(NKECLIPSE){
        		base="${project.projectDir}"
        		target="${netKernel.module.name}"
        	}
        	project.tasks[NKECLIPSE].dependsOn "eclipse"
        	
            configureTask(MODULE) {
                into "${project.buildDir}/${netKernel.module.name}"
                from project.sourceSets.main.output
            }


            configureTask(MODULE_RESOURCES) {
                if(netKernel.sourceStructure.equals(SourceStructure.GRADLE))
                {
                    into "${project.buildDir}/${netKernel.module.name}"
                    from "${project.projectDir}/src/module"
                }
                if(netKernel.sourceStructure.equals(SourceStructure.NETKERNEL))
                {
                    into "${project.buildDir}/${netKernel.module.name}"
                    from "${project.projectDir}/src"
                }
                //Find out what classes were used to build this
                doLast {
                    println ("JAVA CLASSPATH AT BUILD")
                    def groovySources=false
                    
                    def jarsToPack=[]

					//project.tasks.compileJava.classpath.each { f ->
                    project.configurations.runtimeClasspath.each { f ->
                        File fi=f
                        if(fi.absolutePath.matches(".*expanded\\.lib.*"))
                        {   println "REJECTED NETKERNEL MAVEN EXPANDED LIB ${fi.name}"
                        }
                        else if(fi.name.contains("urn.com.ten60.core"))
                        {   println "REJECTED CORE LIB ${fi.name}"
                        }
                        else if(fi.name.contains("urn.com.ten60.netkernel"))
                        {   println "REJECTED CORE LIB ${fi.name}"
                        }
                        else if(fi.name.contains("urn.org.netkernel"))
                        {   println "REJECTED CORE LIB ${fi.name}"
                        }
                        else if(groovySources && fi.absolutePath.matches(".*groovy.*\\.jar"))
                        {   println "REJECTED GROOVY BUILD LIB ${fi.name}"
                        }
                        else
                        {   println "FOUND LIBRARY DEPENDENCY ${fi.name}"
                            jarsToPack.add fi
                        }
                    }

                    if(!jarsToPack.empty)
                    {   println ("PACKING JARS IN MODULE lib/\n======>")
                        jarsToPack=project.files(jarsToPack)
                        jarsToPack.each { f -> println f.name}
                        project.copy {
                            from jarsToPack
                            into "${project.buildDir}/${netKernel.module.name}/lib/"
                        }
                        println ("<======")
                    }
                    println("MODULE ${netKernel.module.name} IS BUILT")

                }
            }

            configureTask(JAR) {
                from project.fileTree(dir: "${project.buildDir}/${netKernel.module.name}")
                duplicatesStrategy 'exclude'
            }

            configureTask(UPDATE_MODULE_XML_VERSION) {
                sourceModuleXml = netKernel.module.moduleFile
                outputModuleXml = project.file("${project.buildDir}/${netKernel.module.name}/module.xml")
            }
            
            //Android DEX tasks
            configureTask(DEX) {
            		doFirst {
            			println "Converting module to DEX bytecode and repacking any DEXed lib/ jars"
		                def f = project.tasks[JAR].archivePath
                		def exe="dx --dex --output=${f}.tmp.jar ${ANDROID_DX_SWITCHES} ${f}"
	                    println exe
	                    def proc = exe.execute();
		                proc.waitFor();
		                println "return code: ${proc.exitValue()}"
		                println "stderr: ${proc.err.text}"
		                println "stdout: ${proc.in.text}"
		                project.copy {
		                    from(project.zipTree("${f}.tmp.jar"))
		                            {
		                                include "classes.dex"
		                            }
		                    into "${project.buildDir}/dexwork/"
		                }
		                project.ant.zip(destfile: "${f}".replaceAll(".jar", ".dex.jar")) {
		                    fileset(dir: "${project.buildDir}/dexwork/") {
		                        include(name: '**/*.*')
		                    }
		                }
		                project.delete "${f}.tmp.jar"
      	
            	}
            
            }
            
            //Android DEX tasks
            configureTask(DEXLIB) {
                doFirst {
                	println "Converting any lib/ jars in the module to DEX bytecode"
	                def ioTree = project.fileTree(dir: "${project.buildDir}/dexwork/lib")
	                ioTree.each { f ->
            			def exe="dx --dex --output=${f}.tmp.jar ${ANDROID_DX_SWITCHES} ${f}"
	                    println exe
	                    def proc = exe.execute();
	                    proc.waitFor();
	                    println "return code: ${proc.exitValue()}"
	                    println "stderr: ${proc.err.text}"
	                    println "stdout: ${proc.in.text}"
	                    project.copy {
	                        from(project.zipTree("${f}.tmp.jar"))
	                                {
	                                    include "classes.dex"
	                                }
	                        into "${project.buildDir}/dexwork/lib/temp"
	                    }
	                    project.copy {
	                        from(project.zipTree("${f}"))
	                        into "${project.buildDir}/dexwork/lib/temp"
	                    }
	                    project.ant.zip(destfile: "${f}".replaceAll(".jar", ".dex.jar")) {
	                        fileset(dir: "${project.buildDir}/dexwork/lib/temp/") {
	                            include(name: '**/*.*')
	                        }
	                    }
	                    project.delete "${f}.tmp.jar"
	                    project.delete "${f}"
	                    project.delete "${project.buildDir}/dexwork/lib/temp"
	
	                }
                }
            }
            
            //Android DEX tasks
            configureTask(DEXUNPACK) {
            	doFirst {
	                project.delete "${project.buildDir}/dexwork/"
	                println "Unpacking built module for DEX conversion"
	                project.copy {
	                    from project.zipTree(project.tasks[JAR].archivePath)
	                    into "${project.buildDir}/dexwork/"
	                }
            	}
            }
        }
    }

    /**
     * Creates task dependencies.
     */
    void createTaskDependencies() {

        if(netKernel.module) {
            project.tasks[JAR].dependsOn MODULE_RESOURCES
            project.tasks[JAR].dependsOn UPDATE_MODULE_XML_VERSION
            project.tasks[MODULE_RESOURCES].dependsOn MODULE
            project.tasks[UPDATE_MODULE_XML_VERSION].dependsOn MODULE_RESOURCES
            
            project.tasks[DEX].dependsOn DEXLIB
            project.tasks[DEXLIB].dependsOn DEXUNPACK
            project.tasks[DEXUNPACK].dependsOn JAR
        }
    }

    /**
     * Final configuration for the project happens here.  Additionally, tasks are created for starting
     * and stopping the specified instances in the build file.
     */
    void afterEvaluate() {
        project.afterEvaluate {
            createNetKernelInstanceTasks()
        }
    }

    /**
     * Creates instance tasks for each NetKernelInstance
     */
    void createNetKernelInstanceTasks() {
        netKernel.instances.each { NetKernelInstance instance ->
            createNetKernelInstanceTasks(instance)
        }
    }

    /**
     * Creates tasks for a specific instance of NetKernel.
     *
     * @param instance instance of NetKernel
     */
    void createNetKernelInstanceTasks(NetKernelInstance instance) {

        String groupName = "NetKernel Instance (${instance.name})"

        //Single Module Deployment Tasks
        String deployTaskName = "deployModule${instance.name}"
        String undeployTaskName = "undeployModule${instance.name}"
        createTask(deployTaskName, DeployModuleTask, "Deploys built module to instance (${instance.name})", groupName)
        createTask(undeployTaskName, UndeployModuleTask, "Undeploys module from instance (${instance.name})", groupName)
        [deployTaskName, undeployTaskName].each { name ->
	        configureTask(name) {
	            netKernelInstance = instance
        		moduleArchiveFile = project.tasks.getByName('jar').archivePath
        		outputs.upToDateWhen { false }		//Always run - don't put this in task constructor else cannot configure netKernelInstance!!
	        }
	    }

        //Starting, Stopping, XUnit, Clean instance Tasks
        String startTaskName = "start${instance.name}"
        String stopTaskName = "stop${instance.name}"
        String cleanTaskName = "clean${instance.name}"
        String cleanDeploymentName = "cleanDeployment${instance.name}"
        String xunitTaskName= "xunit${instance.name}"
        String describeTaskName= "describe${instance.name}"
        createTask(startTaskName, StartNetKernelTask, "Starts NetKernel instance (${instance.name})", groupName)
        createTask(stopTaskName, StopNetKernelTask, "Stops NetKernel instance (${instance.name})", groupName)
        createTask(cleanTaskName, Delete, "Cleans and Deletes the NetKernel instance (${instance.name})", groupName)
        createTask(cleanDeploymentName, Delete, "Cleans the module deployment in etc/modules.d/ for the NetKernel instance (${instance.name})", groupName)
        createTask(xunitTaskName, XUnitTask, "Run XUnit tests on NetKernel instance ${instance.name}", groupName)
        createTask(describeTaskName, DefaultTask, "Describe details for NetKernel instance ${instance.name}", groupName)
        [startTaskName, stopTaskName, xunitTaskName].each { name ->
            configureTask(name) {
                netKernelInstance = instance
            }
        }
        configureTask(cleanTaskName)
        {   delete instance.location
        }
        configureTask(cleanDeploymentName)
        {	
        	doFirst()	{
        		def modulesdDir = new File(instance.getLocation(), "etc/modules.d")
	        	def ft= project.fileTree(modulesdDir) {
	        		include '**/*.xml'
	        	}
	        	delete ft
        		println "Cleaning $modulesdDir"
        	}
        }
        configureTask(describeTaskName)
                {
                    doFirst()
                            {
                                println instance.toString()
                            }
                    outputs.upToDateWhen { false }
                }

        //Apposite Tasks on EE
        String appositeConfigureName=APPOSITE_CONFIGURE+instance.name
        String appositeUpdateName=APPOSITE_UPDATE+instance.name
        String appositeIsUpdatedName=APPOSITE_ISUPDATED+instance.name
        String licenseTaskName= "deployLicense${instance.name}"
        String deleteLicenseDirTaskName= "deleteLicenseDir${instance.name}"
        if(instance.edition==Edition.ENTERPRISE) {
            createTask(appositeConfigureName, ConfigureAppositeTask, "Configures NetKernel (${instance.name}) with packages from Apposite repository", groupName)
            createTask(appositeUpdateName, UpdateAppositeTask, "Updates NetKernel (${instance.name}) from Apposite repository", groupName)
            createTask(appositeIsUpdatedName, UptodateTask, "Verifies that (${instance.name}) is up to date with latest changes from Apposite repository", groupName)
            createTask(licenseTaskName, DeployLicenseTask, "Deploy License(s) to NetKernel instance ${instance.name}", groupName)
            createTask(deleteLicenseDirTaskName, Delete, "Delete License(s) from NetKernel instance ${instance.name}", null)

            [appositeIsUpdatedName, appositeUpdateName].each { name ->
                configureTask(name) {
                    netKernelInstance = instance
                }
            }
            configureTask(appositeConfigureName) {
                apposite = netKernel.apposite
            }
            configureTask(licenseTaskName)
            {   def licensedir=new File(instance.location, "etc/license/")
                def licenses = project.fileTree('.') {
                    include '**/*.lic'
                }
                from licenses.files  //flatten the tree
                into licensedir
            }
            configureTask(deleteLicenseDirTaskName)
            {   def licensedir=new File(instance.location, "etc/license/")
                //Clean out any old licenses
                delete licensedir
            }
            project.tasks[licenseTaskName].dependsOn deleteLicenseDirTaskName

        }
        else log.info "${instance.name} is SE. Apposite tasks not available"

        
        if(instance.freezeConfig!=null)
        {	 //Freeze Instance tasks
            String freezeTaskName = "freeze${instance.name}"
            String publishFrozenTaskName = "publishFrozen${instance.name}"
            String copyBeforeFreezeTaskName = "copyBeforeFreeze${instance.name}"
            String freezeTidyTaskName = "freezeTidy${instance.name}"
            String cleanFreezeTaskName = "cleanFreeze${instance.name}"

        	createTask(freezeTaskName, Jar, "Freezes the NetKernel instance (${instance.name})", groupName)
            createTask(copyBeforeFreezeTaskName, Copy, "Copies instance into freeze staging directory", null)
            createTask(freezeTidyTaskName, FreezeTidyTask, "Cleans up copied instance", null)
            createTask(cleanFreezeTaskName, Delete, "Cleans frozen instance", null)
	        createTask(publishFrozenTaskName, DefaultTask, "Publish frozen NetKernel ${instance.name} into maven repository", groupName)
	        configureTask(publishFrozenTaskName) {
	        	dependsOn project.tasks[freezeTaskName]
	        	dependsOn project.tasks['publish']
	            doFirst() {
	            	println("PUBLISHING ${instance.name} TO MAVEN REPO - " + instance.getFrozenJarFile())
	            }
	            doLast()
	            {   //Clear up the temporary freeze location
	                project.delete instance.getFrozenJarFile()
	                project.delete instance.getFreezeLocation()
	            }
	        }
        	configureTask(freezeTaskName) {
                from instance.getFreezeLocation()
                destinationDirectory = instance.location
                archiveFileName = instance.getFrozenJarFile().name
                archiveBaseName = instance.getFreezeName()
                project.publishing.publications	{
                	FREEZE(MavenPublication)
                	{	
                		groupId=instance.getFreezeGroup()
                		artifactId=instance.getFreezeName()
                		version=instance.getFreezeVersion()
                		artifact	project.tasks[freezeTaskName]
                	}
                }
                
            }
            configureTask(copyBeforeFreezeTaskName) {
                from instance.location
                into instance.getFreezeLocation()
                include "**/*"
            }
            configureTask(freezeTidyTaskName) {
                freezeDirectory = instance.getFreezeLocation()
                installDirectory = instance.location
            }
            configureTask(cleanFreezeTaskName) {
                delete instance.getFrozenJarFile()
                delete instance.getFreezeLocation()
            }
            //Freeze Dependency
            project.tasks[freezeTaskName].dependsOn freezeTidyTaskName
            project.tasks[freezeTidyTaskName].dependsOn copyBeforeFreezeTaskName
        }
        
        //Deploy Collection Tasks
        String deployCollectionName=DEPLOY_COLLECTION+instance.name
        String undeployCollectionName=UNDEPLOY_COLLECTION+instance.name
        createTask(deployCollectionName, DeployCollectionTask, "Deploy the collection of modules from Maven to NetKernel(${instance.name})", groupName)
        createTask(undeployCollectionName, Delete, "Undeploy the collection of modules from NetKernel(${instance.name})", groupName)
        
        //Deploy collection task
        configureTask(deployCollectionName) {
            //deploy = netKernel.deploy
            from project.configurations.nkdeploy
            //Copy the nkdeploy dependencies set up by the Deploy configuration
            def modulesDir = new File(instance.getLocation(), "modules")
            into modulesDir     //Into the modules directory of the thawed target
            //Keep record of each copied file in the copied list in the task
            eachFile { f ->
                name = f.getFile().getName()
                println("Copied $name")
                copied.add(name)
            }
            def modulesd = new File(instance.location, "etc/modules.d/")
            doLast {
                writeModulesd(modulesd, netKernel.deploy);
            }
            outputs.upToDateWhen { false }      //Make sure deployment always works and no cache
        }
        configureTask(undeployCollectionName) {
            def modulesd = new File(instance.location, "etc/modules.d/${netKernel.deploy.collection}.xml")
            delete modulesd
            outputs.upToDateWhen { false }      //Make sure deployment always works and no cache
        }

        //Install NetKernel instance
        String installTaskName = "install${instance.name}"
        createTask(installTaskName, InstallNetKernelTask, "Installs NetKernel instance (${instance.name})", groupName)
        configureTask(installTaskName) {
            netKernelInstance = instance
        }
        project.tasks[installTaskName].dependsOn DOWNLOAD

        //THAW frozen Instance
        if(instance.thawConfig!=null) {
            String thawTaskName = "thaw${instance.name}"
            String thawRepoFetchTaskName = "thawRepoFetch${instance.name}"
            String thawExpandTaskName = "thawExpand${instance.name}"
            String thawConfigureTaskName = "thawConfigure${instance.name}"
            createTask(thawTaskName, Delete, "Thaws/installs frozen NetKernel instance (${instance.name}) from repo", groupName)
            createTask(thawConfigureTaskName, ThawConfigureTask, "Thaws and expands frozen NetKernel instance (${instance.name})", null)
            createTask(thawExpandTaskName, Copy, "Thaws and expands frozen NetKernel instance (${instance.name})", null)
            createTask(thawRepoFetchTaskName, Copy, "Thaws/installs frozen NetKernel instance (${instance.name}) from repo", null)
            configureTask(thawRepoFetchTaskName) {
                from project.configurations["thawrepo"+instance.name]
                def modulesDir = new File(instance.location, "thaw/")
                into modulesDir     //Into the modules directory of the thawed target
                eachFile { f ->
                    //Have to save this file as the "outputs" of this task is the "into" value not the downloaded jar file!
                    netKernel.frozenArchiveFile = new File(modulesDir, f.name)
                }
            }
            configureTask(thawExpandTaskName) {
                from new File(instance.location, "thaw/")
                //Do this to ensure cache invalidation works - but file is set for real in doFirst()
                into instance.location
                include '**/*'

                doFirst() {
                    //Use the archive that was downloaded from the repo now we know its name.
                    from project.zipTree(netKernel.frozenArchiveFile)
                }
            }
            configureTask(thawConfigureTaskName) {
                thawDirInner instance.location
            }
            configureTask(thawTaskName) {
                delete new File(instance.location, "thaw/")
                doLast() {
                    def f = new File(instance.location, netKernel.frozenArchiveFile.name)
                    f.delete()
                }
            }
            //Thaw pipeline dependencies
            project.tasks[thawTaskName].dependsOn thawConfigureTaskName
            project.tasks[thawConfigureTaskName].dependsOn thawExpandTaskName
            project.tasks[thawExpandTaskName].dependsOn thawRepoFetchTaskName
        }

    }

    /**
     * Creates enumeration of possible NetKernel instances. This is done by looping through each edition and
     * constructing a NetKernelInstance for each one.
     *
     * @return internal gradle collection containing NetKernel instances
     */
    NamedDomainObjectContainer<NetKernelInstance> createNetKernelInstances() {
        NamedDomainObjectContainer<NetKernelInstance> instances = project.container(NetKernelInstance)
        //println("CREATING DEFAULT NK INSTANCE")
        if(instances.isEmpty()) {
            def nk = createNetKernelInstance(Edition.ENTERPRISE)
            instances.add nk
        }
        return instances
    }

    /**
     * Creates a NetKernelInstance reference using properties for ports, url and locations.
     *
     * @param edition Edition of NetKernel (SE or EE)
     *
     * @return initialized NetKernelInstance
     */
    NetKernelInstance createNetKernelInstance(Edition edition) {
        String name = "${edition}"
        String netKernelVersion = netKernel.currentMajorReleaseVersion()

        NetKernelInstance instance = new NetKernelInstance(
            name: name,
            edition: edition,
            netKernelVersion: netKernelVersion,
            url: new URL(netKernel.projectProperty(NETKERNEL_INSTANCE_DEFAULT_URL)),
            backendPort: netKernel.projectProperty(NETKERNEL_INSTANCE_BACKEND_PORT) as int,
            frontendPort: netKernel.projectProperty(NETKERNEL_INSTANCE_FRONTEND_PORT) as int,
            location: netKernel.workFile(netKernel.projectProperty(NETKERNEL_INSTANCE_INSTALL_DIR, null, [edition: edition, netKernelVersion: netKernelVersion])),
            jarFileLocation: netKernel.workFile(netKernel.projectProperty(NETKERNEL_INSTANCE_DOWNLOAD_JAR_NAME, null, [edition: edition, netKernelVersion: netKernelVersion])),
            frozenJarFile: netKernel.workFile("freeze/frozen-${name}.jar"),
            frozenLocation: netKernel.workFile("freeze/${name}"),
            project: this.project
        )
        //instance.eggMeetChicken()
        return instance
    }

    /**
     * Private helper method to make task creation more terse.
     *
     * @param name name of task
     * @param type class defining type of task
     * @param description description of what task does
     *
     * @return created task
     */
    private Task createTask(String name, Class type, String description, String group = 'NetKernel') {
        project.tasks.create(
            name: name,
            group: group,
            type: type,
            description: description
        )
    }

    /**
     * Private helper method to make configuration of tasks more terse.
     *
     * @param name name of task
     * @param closure closure used to configure task
     */
    private void configureTask(String name, Closure closure) {
        project.tasks[name].configure(closure)
    }

}
