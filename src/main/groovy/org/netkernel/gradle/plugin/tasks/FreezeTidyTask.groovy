package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.util.FileSystemHelper

/**
 * Created by tab on 02/04/2014.
 */
class FreezeTidyTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def freezeDirInner;
    def installDirInner;

    @org.gradle.api.tasks.TaskAction
    void freeze() {

        //remove license directory
        println "DELETE LICENSE DIR"
        def licenseDir = freezeDirInner + "/etc/license/"
        ant.delete(dir: licenseDir);

        //edit kernel.properties
        println "CLEANING KERNEL.PROPERTIES"
        def propertiesFile = freezeDirInner + "/etc/kernel.properties";
        def pr = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8"));
        String line;
        def sb = new StringBuilder(2048);
        def expandDir = null;
        while (line = pr.readLine()) {
            if (line.startsWith("netkernel.layer0.expandDir")) {
                def i = line.indexOf('=');
                if (i > 0) {
                    expandDir = line.substring(i + 1);
                    def expandDirURI = URI.create(expandDir);
                    def installDirURI = URI.create(installDirInner);
                    def expandDirRelativeURI = installDirURI.relativize(expandDirURI);

                    line = line.substring(0, i + 1) + expandDirRelativeURI.toString();
                }
            }
            sb.append(line);
            sb.append('\n');
        }
        pr.close();
        def fos = new FileOutputStream(propertiesFile);
        def osw = new OutputStreamWriter(fos, "UTF-8");
        osw.write(sb.toString());
        osw.flush();
        fos.close();

        //edit netkernel.sh
        println "CLEANING NETKERNEL.SH"
        def netkernelShFile = freezeDirInner + "/bin/netkernel.sh";
        def pr2 = new BufferedReader(new InputStreamReader(new FileInputStream(netkernelShFile), "UTF-8"));
        String line2;
        def sb2 = new StringBuilder(2048);
        while ((line2 = pr2.readLine()) != null) {   //println line2
            if (line2.startsWith("INSTALLPATH='")) {
                def i = line2.indexOf('\'');
                if (i > 0) {
                    def installPath = line2.substring(i + 1);
                    line2 = "INSTALLPATH='%INSTALLPATH%'";
                }
            }
            sb2.append(line2);
            sb2.append('\n');
        }
        pr2.close();
        def fos2 = new FileOutputStream(netkernelShFile);
        def osw2 = new OutputStreamWriter(fos2, "UTF-8");
        osw2.write(sb2.toString());
        osw2.flush();
        fos2.close();

        //delete expanded dir
        if (expandDir != null) {
            def expandDirURI = URI.create(expandDir);
            def freezeDirURI = new File(freezeDirInner).toURI();
            def installDirURI = URI.create(installDirInner);
            def expandDirRelativeURI = installDirURI.relativize(expandDirURI);
            def freezeExpandDir = freezeDirURI.resolve(expandDirRelativeURI);
//            def freezeExpandDirFile=new File(freezeExpandDir).toString()+"/*";
            println "DELETING EXPANDDIR ${freezeExpandDir}"
//            ant.delete(dir: freezeExpandDir, includes: '**')
            project.delete(project.fileTree(dir: freezeExpandDir, include: "**/*"))
        }

        //delete package cache dir
        def packageCacheDir = freezeDirInner + "/package-cache/"
        println "DELETING PACKAGE CACHE ${packageCacheDir}"
        ant.delete(dir: packageCacheDir);

        //delete log dir
        def logDir = freezeDirInner + "/log/"
        println "DELETING LOGS ${logDir}"
        ant.delete(dir: logDir);
    }
}
