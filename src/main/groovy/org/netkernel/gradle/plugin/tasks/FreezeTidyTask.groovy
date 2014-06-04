package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

/**
 * Tidys up copied NetKernel instance by removing license, updating expandDir, etc.
 *
 * @author tab on 02/04/2014.
 */
@Slf4j
class FreezeTidyTask extends DefaultTask {

    @Input
    File freezeDirectory

    @Input
    File installDirectory

    @org.gradle.api.tasks.TaskAction
    void freeze() {

        //remove license directory
        println "DELETE LICENSE DIR"
        File licenseDir = new File(freezeDirectory, "/etc/license/")
        ant.delete(dir: licenseDir)

        //edit kernel.properties
        println "CLEANING KERNEL.PROPERTIES"
        File propertiesFile = new File(freezeDirectory, "/etc/kernel.properties")
        def pr = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8"))
        String line
        def sb = new StringBuilder(2048)
        def expandDir = null
        while (line = pr.readLine()) {
            if (line.startsWith("netkernel.layer0.expandDir")) {
                def i = line.indexOf('=')
                if (i > 0) {
                    expandDir = line.substring(i + 1)
                    def expandDirURI = URI.create(expandDir)
                    def installDirURI = installDirectory.toURI()
                    def expandDirRelativeURI = installDirURI.relativize(expandDirURI)

                    line = line.substring(0, i + 1) + expandDirRelativeURI.toString()
                }
            }
            sb.append(line)
            sb.append('\n')
        }
        pr.close()
        def fos = new FileOutputStream(propertiesFile)
        def osw = new OutputStreamWriter(fos, "UTF-8")
        osw.write(sb.toString())
        osw.flush()
        fos.close()

        //edit netkernel.sh
        println "CLEANING NETKERNEL.SH"
        File netkernelShFile = new File(freezeDirectory, "/bin/netkernel.sh")
        def pr2 = new BufferedReader(new InputStreamReader(new FileInputStream(netkernelShFile), "UTF-8"))
        String line2
        def sb2 = new StringBuilder(2048)
        while ((line2 = pr2.readLine()) != null) {   //println line2
            if (line2.startsWith("INSTALLPATH='")) {
                def i = line2.indexOf('\'')
                if (i > 0) {
                    def installPath = line2.substring(i + 1)
                    line2 = "INSTALLPATH='%INSTALLPATH%'"
                }
            }
            sb2.append(line2)
            sb2.append('\n')
        }
        pr2.close()
        def fos2 = new FileOutputStream(netkernelShFile)
        def osw2 = new OutputStreamWriter(fos2, "UTF-8")
        osw2.write(sb2.toString())
        osw2.flush()
        fos2.close()

        //delete expanded dir
        if (expandDir != null) {
            def expandDirURI = URI.create(expandDir)
            def freezeDirURI = freezeDirectory.toURI()
            def installDirURI = installDirectory.toURI()
            def expandDirRelativeURI = installDirURI.relativize(expandDirURI)
            def freezeExpandDir = freezeDirURI.resolve(expandDirRelativeURI)
//            def freezeExpandDirFile=new File(freezeExpandDir).toString()+"/*"
            println "DELETING EXPANDDIR ${freezeExpandDir}"
//            ant.delete(dir: freezeExpandDir, includes: '**')
            project.delete(project.fileTree(dir: freezeExpandDir, include: "**/*"))
        }

        //delete package cache dir
        File packageCacheDir = new File(freezeDirectory, '/package-cache')
        println "DELETING PACKAGE CACHE ${packageCacheDir}"
        ant.delete(dir: packageCacheDir)

        //delete log dir
        def logDir = new File(freezeDirectory, '/log')
        println "DELETING LOGS ${logDir}"
        ant.delete(dir: logDir)
    }
}
