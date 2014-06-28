package org.netkernel.gradle.plugin

import org.gradle.api.*
import org.gradle.api.artifacts.maven.MavenResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.netkernel.gradle.plugin.model.*
import org.netkernel.gradle.plugin.tasks.*

import static org.netkernel.gradle.plugin.model.PropertyHelper.*
import static org.netkernel.gradle.plugin.tasks.TaskName.*

/**
 * A plugin to Gradle to manage NetKernel modules, builds, etc.
 */
class NetKernelPlugin implements Plugin<Project> {

    Project project

    // The primary model class for the build.
    NetKernelExtension netKernel

    void apply(Project project) {
        this.project = project

        // TODO - Do we always want to apply the groovy plugin?  What about other languages like kotlin, scala, etc.?
        project.apply plugin: 'groovy'
        project.apply plugin: 'maven'

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

        ['freeze', 'thaw', 'provided'].each { name ->
            project.configurations.create(name)
        }

        // This is so that provided dependencies are on the classpath, but are filtered out during the build
        project.configurations.compile.extendsFrom(project.configurations.provided)

        netKernel = project.extensions.create("netkernel", NetKernelExtension, project)
        netKernel.instances = createNetKernelInstances()

        // Determine project structure
        if (new File(project.projectDir, "src/module.xml").exists()) {
            netKernel.sourceStructure = SourceStructure.NETKERNEL
        } else {
            netKernel.sourceStructure = SourceStructure.GRADLE
        }

        // TODO - I don't know what this does
        //        project.artifacts {
//            freeze project.tasks[FREEZE_JAR].outputs
//        }

        switch (netKernel.sourceStructure) {
            case SourceStructure.NETKERNEL:
                def baseDir = new File(project.projectDir, 'src/')
                //Configure the javaCompiler
                def fileTree = project.fileTree(dir: baseDir, includes: ['**/*.java'])
                //fileTree.visit { f ->  println f }
                project.tasks.compileJava.configure {
                    source = fileTree
                }
                //Configure the groovyCompiler
                fileTree = project.fileTree(dir: new File(project.projectDir, 'src/'), includes: ['**/*.groovy'])
                project.tasks.compileGroovy.configure {
                    source = fileTree
                }

                // TODO - Make this work for other languages (scala, kotlin, etc.)

                netKernel.module = new Module(project.file('src/module.xml'))

                //Add any libs to classpath
                def libDir = new File(baseDir, "lib/")
                if (libDir.exists()) {
                    def libTree = project.fileTree(dir: libDir, includes: ['**/*.jar'])
                    libTree.visit { f ->
                        //println "lib/ DEPENDENCY ADDED: ${f}"
                    }
                    project.dependencies.add("compile", libTree)
                }

                break;
            case SourceStructure.GRADLE:
                if (project.file('src/module/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/module/module.xml'))
                } else if (project.file('src/main/resources/module.xml').exists()) {
                    netKernel.module = new Module(project.file('src/main/resources/module.xml'))
                }
                break;
        }

        if (!netKernel.module) {
            throw new InvalidUserDataException("Could not find module.xml in the project.")
        }

        // If the project has a netKernelVersion specified, override the value in the module.xml
        if (project.version == 'unspecified') {
            project.version = netKernel.module.version
        } else {
            netKernel.module.version = project.version
        }

        //Set Maven Artifact name and netKernelVersion
        //See http://www.gradle.org/docs/current/userguide/maven_plugin.html#sec:maven_pom_generation
        project.archivesBaseName = netKernel.module.URIDotted
    }

    /**
     * Creates all of the tasks for this plugin.  Some tasks may be disabled depending on
     * what is provided by the build.gradle file.
     */
    void createTasks() {

//        createTask(CLEAN_SE_JAR, CleanDownloadedJarTask, 'Removes downloaded SE jar')

        createTask(CONFIGURE_APPOSITE, ConfigureAppositeTask, 'Configures apposite repository')

        createTask(COPY_BEFORE_FREEZE, Copy, 'Copies NetKernel installation into freeze directory')

        createTask(CREATE_APPOSITE_PACKAGE, CreateAppositePackageTask, 'Creates apposite package')

        createTask(DOWNLOAD_EE, DownloadNetKernelTask, 'Downloads NetKernel EE edition')

        createTask(DOWNLOAD_SE, DownloadNetKernelTask, 'Downloads NetKernel SE edition')

        createTask(FREEZE_DELETE, Delete, "Deletes frozen NetKernel instance")

        createTask(FREEZE_JAR, Jar, 'Creates zip file of frozen NetKernel instance')

        createTask(FREEZE_TIDY, FreezeTidyTask, 'Cleans up copied NetKernel instance during freeze')

        createTask(INSTALL_FREEZE, Upload, "Installs frozen NetKernel instance into maven repository")

        createTask(MODULE, Copy, "Copies built classes to build folder")

        createTask(MODULE_RESOURCES, ModuleResourcesTask, 'Copies module resources (module.xml, etc.) to build folder')

        createTask(THAW, Copy, "Copies frozen NetKernel instance into thaw directory")
            .setEnabled(project.configurations.thaw.files.size() == 1)

        createTask(THAW_CONFIGURE, ThawConfigureTask, "Thaws configuration for NetKernel instance")

        createTask(THAW_DELETE_INSTALL, Delete, "Deletes thawed installation directory")

        createTask(THAW_EXPAND, Copy, "Expands thawed NetKernel instance into thaw installation directory")

        createTask(UPDATE_MODULE_XML_VERSION, UpdateModuleXmlVersionTask, 'Updates version in module xml to match project version')
            .setEnabled(netKernel.module.versionOverridden)

        /*        project.task('freezePublish', type: org.gradle.api.publish.maven.tasks.PublishToMavenLocal) {
            publication {
                from freezeDir
                artifact 'frozen.zip'
            }
        } */

    }

    /**
     * Any custom configuration for the tasks is done here.  Specific data needed by the tasks
     * is provided by the backing model classes (e.g. {@see NetKernelExtension})
     */
    void configureTasks() {

        configureTask(COPY_BEFORE_FREEZE) {
            from netKernel.installationDirectory
            into netKernel.freezeDirectory
            include "**/*"
        }

        configureTask(FREEZE_TIDY) {
            freezeDirectory = netKernel.freezeDirectory
            installDirectory = netKernel.installationDirectory
        }

        configureTask(FREEZE_JAR) {
            from netKernel.freezeDirectory
            destinationDir = netKernel.destinationDirectory
            archiveName = 'frozen.zip'
        }

        configureTask(INSTALL_FREEZE) {
            // TODO - Introduce constants for configuration 'freeze', etc.
            configuration = project.configurations.freeze
            repositories {
                mavenInstaller()
            }
            repositories.withType(MavenResolver) {
                pom.groupId = 'org.netkernel'
                pom.version = '1.1.1'
                pom.artifactId = 'frozenInstance'
                pom.scopeMappings.mappings.clear()
            }
        }

        if (project.tasks[THAW].enabled) {
            configureTask(THAW) {
                into netKernel.thawDirectory
                from project.zipTree(project.configurations.thaw.singleFile)
            }
        }

        configureTask(FREEZE_DELETE) {
            delete netKernel.freezeDirectory
        }

        // TODO - Figure out what this task is doing...
        //configureTask(THAW_DELETE_INSTALL) {
        //    delete project.netkernel.thawInstallationDirectory
        //}

        configureTask(THAW_EXPAND) {
            from project.zipTree(netKernel.frozenArchiveFile)
            into netKernel.thawInstallationDirectory
            include '**/*'
        }

        configureTask(THAW_CONFIGURE) {
            thawDirInner netKernel.thawInstallationDirectory
        }


        configureTask(DOWNLOAD_SE) {
            downloadConfig = netKernel.download.se
            edition = Edition.STANDARD
            netKernelVersion = netKernel.currentMajorReleaseVersion()
            destinationFile = netKernel.workFile("download/${netKernel.distributionJarFile(edition, netKernelVersion)}")
        }

        configureTask(DOWNLOAD_EE) {
            downloadConfig = netKernel.download.ee
            edition = Edition.ENTERPRISE
            netKernelVersion = netKernel.currentMajorReleaseVersion()
            destinationFile = netKernel.workFile("download/${netKernel.distributionJarFile(edition, netKernelVersion)}")
        }

        configureTask(MODULE) {
            into "${project.buildDir}/${netKernel.module.name}"
            from project.sourceSets.main.output
        }

        configureTask(MODULE_RESOURCES) {
            into "${project.buildDir}/${netKernel.module.name}"
            if (netKernel.sourceStructure == SourceStructure.GRADLE) {
                from "${project.projectDir}/src/module"
                from "${project.projectDir}/src/main/resources"
            } else {
                from "${project.projectDir}/src"
            }
        }

        // TODO: Rethink for multi modules
        configureTask(JAR) {
            from project.fileTree(dir: "${project.buildDir}/${netKernel.module.name}")
            duplicatesStrategy 'exclude'
        }

        configureTask(UPDATE_MODULE_XML_VERSION) {
            sourceModuleXml = netKernel.module.moduleFile
            outputModuleXml = project.file("${project.buildDir}/${netKernel.module.name}/module.xml")
        }

    }

    /**
     * Creates task dependencies.
     */
    void createTaskDependencies() {

        project.tasks[FREEZE_DELETE].dependsOn FREEZE_JAR
        project.tasks[FREEZE_JAR].dependsOn FREEZE_TIDY
        project.tasks[FREEZE_TIDY].dependsOn COPY_BEFORE_FREEZE
        project.tasks[JAR].dependsOn MODULE_RESOURCES
        project.tasks[JAR].dependsOn UPDATE_MODULE_XML_VERSION
        project.tasks[MODULE].dependsOn COMPILE_GROOVY
        project.tasks[MODULE_RESOURCES].dependsOn MODULE
        project.tasks[THAW_CONFIGURE].dependsOn THAW_EXPAND
        project.tasks[THAW_EXPAND].dependsOn THAW_DELETE_INSTALL
        project.tasks[UPDATE_MODULE_XML_VERSION].dependsOn MODULE_RESOURCES

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

        String groupName = "NetKernel Instance (${instance})"

        String startTaskName = "start${instance.name}"
        String stopTaskName = "stop${instance.name}"
        String installTaskName = "install${instance.name}"
        String deployTaskName = "deployTo${instance.name}"
        String undeployTaskName = "undeployFrom${instance.name}"

        String freezeTaskName = "freeze${instance.name}"
        String copyBeforeFreezeTaskName = "copyBeforeFreeze${instance.name}"
        String freezeTidyTaskName = "freezeTidy${instance.name}"


        createTask(startTaskName, StartNetKernelTask, "Starts NetKernel instance (${instance.name})", groupName)
        createTask(stopTaskName, StopNetKernelTask, "Stops NetKernel instance (${instance.name})", groupName)
        createTask(installTaskName, InstallNetKernelTask, "Installs NetKernel instance (${instance.name})", groupName)
        createTask(deployTaskName, DeployToNetKernelTask, "Deploys module(s) to instance (${instance.name})", groupName)
        createTask(undeployTaskName, UndeployFromNetKernelTask, "Undeploys module(s) from instance (${instance.name})", groupName)

        // Tasks related to freezing and thawing instance
        createTask(freezeTaskName, Jar, "Freezes the NetKernel instance (${instance.name})", groupName)
        createTask(copyBeforeFreezeTaskName, Copy, "Copies instance into freeze staging directory", groupName)
        createTask(freezeTidyTaskName, FreezeTidyTask, "Cleans up copied instance", groupName)

        [startTaskName, stopTaskName, installTaskName, deployTaskName, undeployTaskName].each { name ->
            configureTask(name) {
                netKernelInstance = instance
            }
        }

        [deployTaskName, undeployTaskName].each { name ->
            configureTask(name) {
                moduleArchiveFile = project.tasks.getByName('jar').archivePath
            }
        }

        configureTask(freezeTaskName) {
            from instance.location
            destinationDir = netKernel.workFile("freeze")
            archiveName = "frozen-${instance.name}.jar"
        }

        configureTask(copyBeforeFreezeTaskName) {
            from instance.location
            into netKernel.workFile("freeze/${instance.name}")
            include "**/*"
        }

        configureTask(freezeTidyTaskName) {
            freezeDirectory = netKernel.workFile("freeze/${instance.name}")
            installDirectory = instance.location
        }

//        // TODO - I don't think this works as expected.  Should be a clean per instance perhaps?
//        def applyCleanAllTask(def project, ExecutionConfig config) {
//            project.task("cleanAll${config.name}", type: CleanAllTask)
//                {
//                    executionConfig = config
//                }
//        }

        // TODO - Add task dependencies for instance tasks by from previous code:
//                project.tasks."${startNKJarName}".dependsOn "downloadNK${config.relType}"
//                project.tasks."${installNKJarName}".dependsOn startNKJarName
//                    //project.tasks."${initDaemonDirName}".dependsOn installNKJarName

        project.tasks[freezeTaskName].dependsOn freezeTidyTaskName
        project.tasks[freezeTidyTaskName].dependsOn copyBeforeFreezeTaskName
    }

    /**
     * Creates enumeration of possible NetKernel instances. This is done by looping through each edition and
     * constructing a NetKernelInstance for each one.
     *
     * @return internal gradle collection containing NetKernel instances
     */
    NamedDomainObjectContainer<NetKernelInstance> createNetKernelInstances() {
        NamedDomainObjectContainer<NetKernelInstance> instances = project.container(NetKernelInstance)

        Edition.values().each { Edition edition ->
            instances.add createNetKernelInstance(edition)
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
            jarFileLocation: netKernel.workFile(netKernel.projectProperty(NETKERNEL_INSTANCE_DOWNLOAD_JAR_NAME, null, [edition: edition, netKernelVersion: netKernelVersion]))
        )

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
