package org.netkernel.gradle.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.netkernel.gradle.nk.ExecutionConfig
import org.netkernel.gradle.nk.ReleaseType
import org.netkernel.gradle.plugin.tasks.*
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.ModuleHelper
/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {
    
	public static final String NETKERNELSRC="NETKERNELSRC"
	public static final String GRADLESRC="GRADLESRC"
	
	def fsHelper = new FileSystemHelper()
    ModuleHelper moduleHelper = null;
    
    def buildJarInstallerExecutionConfig(def project, def type) {
        def config = new ExecutionConfig()

        project.configure(config, {
            name = "${type}Jar"
            relType = type
            release = ReleaseType.CURRENT_MAJOR_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/${type}-${release}")
            installJar = "1060-NetKernel-${type}-${release}.jar"
            mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
        })

        config
    }

    def buildInstalledExecutionConfig(def project, def type) {
        def config = new ExecutionConfig()

        project.configure(config, {
            name = "${type}"
            relType = type
            release = ReleaseType.CURRENT_MAJOR_RELEASE
            directory = fsHelper.dirInGradleHomeDirectory("netkernel/install/${type}-${release}")
            supportsDaemonModules = true
        })

        config
    }

    def gatherExecutionConfigs(def project, def envs) {
        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKSE))
        envs.add(buildJarInstallerExecutionConfig(project, ReleaseType.NKEE))
        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKSE))
        envs.add(buildInstalledExecutionConfig(project, ReleaseType.NKEE))
    }

    def applyCleanAllTask(def project, ExecutionConfig config)
    {		project.task("cleanAll${config.name}", type: CleanAllTask)
    		{	executionConfig = config
    		}
    }
    
    def installExecutionConfigTasks(def project, ExecutionConfig config) {
        def startNKJarName = "start${config.name}"
        def installNKJarName = "install${config.name}"

        switch(config.mode) {
            case ExecutionConfig.Mode.NETKERNEL_INSTALL :

                project.task(startNKJarName, type: StartNetKernelTask) {
                    configName = config.name
                }

                project.task(installNKJarName, type: InstallNetKernelTask) {
                    configName = config.name
                }

                project.tasks."${startNKJarName}".dependsOn "downloadNK${config.relType}"
                project.tasks."${installNKJarName}".dependsOn startNKJarName

                break;
            case ExecutionConfig.Mode.NETKERNEL_FULL :
                def startNKName = "start${config.name}"

                project.task(startNKName, type: StartNetKernelTask) {
                    configName = config.name
                }

                if(config.supportsDaemonModules) {
                    def initDaemonDirName = "initDaemonDir${config.name}"
                    def deployDaemonModuleName = "deployDaemonModule${config.name}"
                    def undeployDaemonModuleName = "undeployDaemonModule${config.name}"

                    project.task(initDaemonDirName, type: InitializeDaemonDirTask) {
                        configName = config.name
                    }

                    //project.tasks."${initDaemonDirName}".dependsOn installNKJarName

                    project.task(deployDaemonModuleName, type: DeployDaemonModuleTask) {
                        configName = config.name
                    }

                    project.task(undeployDaemonModuleName, type: Delete) {
                        delete "${config.directory}/etc/modules.d/${project.name}.xml"
                    }
                }

                break;
        }
    }

    void apply(Project project) {
        project.apply plugin: 'groovy'

        //Has the user set up maven - if not then add it
        def mavenPlugin=project.getPlugins().findPlugin('maven')
        if(mavenPlugin==null) {
            println "Adding Maven Plugin"
            project.apply plugin: 'maven'
        }
        else
        {   println "Found maven plugin - repository configuration responsibility accepted by user"
        }


        def envs = project.container(ExecutionConfig)
        gatherExecutionConfigs(project, envs)

        def extension = project.extensions.create("netkernel", NetKernelExtension, project, envs)

        project.task('createAppositePackage', type: CreateAppositePackage){

        }

        project.task('downloadNKSE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.se
        }

        project.task('downloadNKEE', type: DownloadNetKernelTask) {
            downloadConfig = extension.download.ee
            // TODO: Discuss with 1060
            releaseDir = 'ee'
            release = DownloadNetKernelTask.NKEE
        }

        // Module-specific tasks

        def sourceStructure
        def projectDir=project.projectDir;
        if (new File(projectDir,"src/module.xml").exists())
        {   sourceStructure=NETKERNELSRC
        }
        else
        {   sourceStructure=GRADLESRC
        }
        //println("sourceStructure="+sourceStructure)

        project.ext.nkModuleIdentity=null

        switch(sourceStructure)
        {   case NETKERNELSRC:
                def baseDir=new File(projectDir,'src/');
        		//Configure the javaCompiler
        		def fileTree=project.fileTree(dir:baseDir, includes:['**/*.java'] );
                //fileTree.visit { f ->  println f }
                project.tasks.compileJava.configure {
                    source=fileTree
                }
                //Configure the groovyCompiler
        		fileTree=project.fileTree(dir:new File(projectDir,'src/'), includes:['**/*.groovy'] );
                project.tasks.compileGroovy.configure {
                    source=fileTree
                }
                moduleHelper=new ModuleHelper("${project.projectDir}/src/module.xml")

                //Add any libs to classpath
                def libDir=new File(baseDir, "lib/")
                if(libDir.exists())
                {   def libTree=project.fileTree(dir:libDir, includes:['**/*.jar'] );
                    libTree.visit{ f ->
                        //println "lib/ DEPENDENCY ADDED: ${f}"
                    }
                    project.dependencies.add("compile", libTree)
                }

                break;
            case GRADLESRC:
                if (project.file("${project.projectDir}/src/module/module.xml").exists()) {
                    moduleHelper=new ModuleHelper("${project.projectDir}/src/module/module.xml")
                }
                if (project.file("${project.projectDir}/src/main/resources/module.xml").exists()) {
                    moduleHelper=new ModuleHelper("${project.projectDir}/src/main/resources/module.xml")
                }
            break;
        }

        //Set up module identity and maven artifact
        project.ext.nkModuleIdentity=moduleHelper.getModuleName()

        //Set Maven Artifact name and version
        //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
        project.archivesBaseName=moduleHelper.getModuleURIDotted()
        project.version=moduleHelper.getModuleVersion()

        //println "MODULE TARGET ${project.ext.nkModuleIdentity}"
        //println("Finished configuring srcStructure")

        project.task('module', type: Copy) {
            into "${project.buildDir}/${project.ext.nkModuleIdentity}"
            from project.sourceSets.main.output
        }
        project.tasks.module.dependsOn "compileGroovy"
        
        project.task('moduleResources', type: Copy) {
            if(sourceStructure.equals(GRADLESRC))
            {
            into "${project.buildDir}/${project.ext.nkModuleIdentity}"
            from "${project.projectDir}/src/module"
            }
            if(sourceStructure.equals(NETKERNELSRC))
            {
                into "${project.buildDir}/${project.ext.nkModuleIdentity}"
                from "${project.projectDir}/src"
            }
            //Find out what classes were used to build this
            doLast {
                println ("JAVA/GROOVY CLASSPATH AT BUILD")
                def groovySources=false
                //println "FINDING GROOVY SOURCES"
                project.tasks.compileGroovy.source.each { File s ->
                   if(s.name.endsWith(".groovy"))
                   {    groovySources=true
                        println "FOUND GROOOOOOVY SO WILL REJECT groovy*.jar"
                        return
                   }
                }
                def jarsToPack=[]
                project.tasks.compileJava.classpath.each { f ->
                    File fi=f
                    if(fi.absolutePath.matches(".*expanded\\.lib.*"))
                    {   println "REJECTED NETKERNEL MAVEN EXPANDED LIB ${fi.name}"
                    }
                    else if(fi.absolutePath.contains("urn.com.ten60.core"))
                    {   //println "CORE ${fi.name}"
                    }
                    else if(fi.absolutePath.contains("urn.org.netkernel"))
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
                        into "${project.buildDir}/${project.ext.nkModuleIdentity}/lib/"
                    }
                    println ("<======")
                }
                println("MODULE IS BUILT")

            }
        }
        project.tasks.moduleResources.dependsOn "module"
        
        // TODO: Rethink this for multi modules

        project.tasks.jar.configure {
            destinationDir=project.file("${project.buildDir}/modules")
            archiveName=project.ext.nkModuleIdentity+".jar"
            from project.fileTree(dir:"${project.buildDir}/${project.ext.nkModuleIdentity}")
            duplicatesStrategy 'exclude'
        }
        
        project.tasks.jar.dependsOn 'moduleResources'

        project.afterEvaluate {
            project.netkernel.envs.each { c ->
                installExecutionConfigTasks(project, c)
                applyCleanAllTask(project ,c)
            }
        }

        addNetKernelConfiguration(project)


    }
    
    def addNetKernelConfiguration(Project project) {
      /*  project.sourceSets {
            project.configure([main, test]) {
                project.configure([java,groovy]) {
                    srcDirs = [project.projectDir]
                }
            }
        } */

    }
}
