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
        project.apply plugin: 'maven'

        def envs = project.container(ExecutionConfig)
        gatherExecutionConfigs(project, envs)

        def extension = project.extensions.create("netkernel", NetKernelExtension, project, envs)
        
        
        project.configurations.create("freeze")
        project.configurations.create("thaw")

        //FREEZE SETUP

        //BRIAN how do we parameterise this well?
        def configName = "SE"
        def config = project.netkernel.envs[configName]
        def installationDir = config.directory
        def dest = fsHelper.dirInGradleHomeDirectory("netkernel")
        def freezeDir = fsHelper.dirInGradleHomeDirectory("netkernel/freeze")
        def thawDir = fsHelper.dirInGradleHomeDirectory("netkernel/thaw")
        
        project.task('copyBeforeFreeze', type: Copy) {
            from installationDir
            into freezeDir
            include "**/*"
        }

        project.task('freezeTidy', type: FreezeTidyTask){
            freezeDirInner=freezeDir;
            installDirInner=installationDir;
        }
        project.tasks.freezeTidy.dependsOn "copyBeforeFreeze"

        project.task('freezeJar', type: org.gradle.api.tasks.bundling.Jar ){
            from freezeDir
            destinationDir= new File(dest)
            archiveName=  "frozen.zip"
        }
        project.tasks.freezeJar.dependsOn "freezeTidy"
        
        project.artifacts {
            freeze project.tasks.freezeJar
        }
        
/*        project.task('freezePublish', type: org.gradle.api.publish.maven.tasks.PublishToMavenLocal) {
            publication {
                from freezeDir
                artifact 'frozen.zip'
            }
        } */
        
        project.task('installFreeze', type: org.gradle.api.tasks.Upload ) {
            configuration = project.configurations.freeze

            repositories {
                mavenInstaller()
            }
        }
        
        project.configure([project.tasks.installFreeze, project.tasks.uploadFreeze]) {
            repositories.withType(org.gradle.api.artifacts.maven.MavenResolver) {
                pom.groupId = "org.netkernel"
                pom.version = "1.1.1"
                pom.artifactId = "frozenInstance"
                pom.scopeMappings.mappings.clear()
            }
        }
        
        project.task('thaw', type: Copy) {
            onlyIf {
                project.configurations.thaw.files.size() > 0
            }
            
            doFirst {
                if(project.configurations.thaw.files.size() != 1) {
                    throw new org.gradle.api.InvalidUserDataException("Like Highlander, there can only be one")
                }
            }
            
            into thawDir
            from {
                project.zipTree(project.configurations.thaw.singleFile)
            }
        }

        project.task('freezeDelete', type: Delete ){
            delete freezeDir
        }
        project.tasks.freezeDelete.dependsOn "freezeJar"

        //BRIAN how do we maven publish?
        /*
        project.task('freezePublish', type: org.gradle.api.publish.maven.MavenPublication ){
        }
        project.tasks.freezePublish.dependsOn "freezeJar"
        */

        // THAW SETUP

        //BRIAN how to we retrieve from maven?

        def thawInstallationDir=installationDir+"2"
        project.task('thawDeleteInstall', type: Delete ){
            delete thawInstallationDir
        }

        def frozenJar=dest+"/frozen.zip";
        project.task('thawExpand', type: Copy) {
            from(project.zipTree(frozenJar))
            into thawInstallationDir
            include "**/*"
        }
        project.tasks.thawExpand.dependsOn "thawDeleteInstall"

        project.task('thawConfigure', type: ThawConfigureTask){
            thawDirInner=thawInstallationDir;
        }
        project.tasks.thawConfigure.dependsOn "thawExpand"



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
