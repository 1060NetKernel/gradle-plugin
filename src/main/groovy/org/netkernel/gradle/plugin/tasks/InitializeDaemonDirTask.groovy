package org.netkernel.gradle.plugin.tasks
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.util.FileSystemHelper

class InitializeDaemonDirTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def configName
    
    @TaskAction
    def checkInstallation() {
        // The current assumption is that the installation we are initializing
        // has been created by a dependency between this and the installation task

        def config = project.netkernel.envs[configName]
        
        def dir = "${config.directory}/etc/modules.d"
        
        println "Checking on ${dir}"
        
        if(!fsHelper.dirExists(dir) && !fsHelper.createDirectory(dir)) {
            throw new IllegalStateException("Could not create Daemon Directory: ${dir}")
        }
                
        def loc = []
                
        def kernPropsFile = new File("${config.directory}/etc/kernel.properties")
        kernPropsFile.splitEachLine("=") { line ->
            if(line[0].equals("netkernel.init.modulesdir")) {
                loc.add(line[1])
            }
        }
                
        println "Done processing kernel.properties file."
        println loc
                
        if(loc.size() == 0) {
            kernPropsFile.append("netkernel.init.modulesdir=etc/modules.d/")
        } else {
            if(loc.size() == 1) {
                println "Daemon dir already configured: ${loc.get(0)}"
            } else {
                println "Warning: Too Many References in kernel.properties file."
                println "Last reference will override the others."
                println "Locations: ${loc}"
            }
        }
    }
}