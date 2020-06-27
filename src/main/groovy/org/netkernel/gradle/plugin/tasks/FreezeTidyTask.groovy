package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFile;
import org.apache.tools.ant.taskdefs.condition.Os

/**
 * Tidys up copied NetKernel instance by removing license, updating expandDir, etc.
 *
 * @author tab on 02/04/2014.
 */
@Slf4j
class FreezeTidyTask extends DefaultTask {

	@Optional				//This is a hack as @File fails as gradle tries to validate the File and even though the object is fine it does not yet exist and will be created in the task!!
    File freezeDirectory

    @Optional				//Ditto
    File installDirectory

    @org.gradle.api.tasks.TaskAction
    void freeze() {
		println("------------TIDYING BEFORE FREEZING-------------")

        // Delete license directory
        //delete(new File(freezeDirectory, "/etc/license"))

        //edit kernel.properties
        log.debug "Cleaning up kernel.properties"

        File freezeExpandDir
        /*
        File propertiesFile = new File(freezeDirectory, "etc/kernel.properties")
        propertiesFile.text = propertiesFile.text.replaceAll(/netkernel.layer0.expandDir=(.*)/,"netkernel.layer0.expandDir=")
        */
        /*
        propertiesFile.text = propertiesFile.text.replaceAll(/netkernel.layer0.expandDir=(.*)/) { match ->
            def fileuri=""
            if(isWindows())
            {   fileuri="file:///"
            }
            URI expandDirURI = URI.create(fileuri+match[1])
            String expandDirPath = URI.create(fileuri+installDirectory.absolutePath).relativize(expandDirURI) as String
            freezeExpandDir = new File(freezeDirectory, expandDirPath)
            return "netkernel.layer0.expandDir=${freezeExpandDir}"
        }
        */

        log.debug "Cleaning up netkernel.sh"

        if(isWindows())
        {   File netkernelShFile = new File(freezeDirectory, "/bin/netkernel.bat")
            netkernelShFile.text = netkernelShFile.text.replaceAll(/INSTALLPATH=".*?"/,"INSTALLPATH=\"%INSTALLPATH%\"")
        }
        else
        {   File netkernelShFile = new File(freezeDirectory, "/bin/netkernel.sh")
            netkernelShFile.text = netkernelShFile.text.replaceAll(/INSTALLPATH='.*?'/,"INSTALLPATH='%INSTALLPATH%'")
        }


        //  Delete freeze expand dir
        if(freezeExpandDir) {
            delete(freezeExpandDir)
        }
        
        // delete package cache dir
        delete(new File(freezeDirectory, "lib/expanded"))


        // delete package cache dir
        delete(new File(freezeDirectory, "package-cache"))

        // delete log dir
        delete(new File(freezeDirectory, "log"))
    }

    private void delete(File directory) {
        println "Deleting ${directory}"
        project.delete(directory)
    }

    private boolean isWindows()
    {   return Os.isFamily(Os.FAMILY_WINDOWS)
    }
}
