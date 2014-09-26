package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.netkernel.gradle.plugin.util.FileSystemHelper

/**
 * Created by tab on 02/04/2014.
 */
class ThawConfigureTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    def thawDirInner;

    @org.gradle.api.tasks.TaskAction
    void thaw() {

        //edit netkernel.sh
        println "CONFIGURING NETKERNEL.SH"
        def netkernelShFile=new File(thawDirInner ,"/bin/netkernel.sh");
        def pr2 = new BufferedReader(new InputStreamReader(new FileInputStream(netkernelShFile),"UTF-8"));
        String line2;
        def sb2=new StringBuilder(2048);
        while ((line2=pr2.readLine())!=null)
        {   //println line2
            if (line2.startsWith("INSTALLPATH='"))
            {
                def i=line2.indexOf('\'');
                if (i>0)
                {   def installPath=line2.substring(i+1);
                    line2="INSTALLPATH='"+thawDirInner+"'";
                }
            }
            sb2.append(line2);
            sb2.append('\n');
        }
        pr2.close();
        def fos2=new FileOutputStream(netkernelShFile);
        def osw2=new OutputStreamWriter(fos2,"UTF-8");
        osw2.write(sb2.toString());
        osw2.flush();
        fos2.close();

        //recreate license dir
        println "MKDIR LICENSE"
        def licenseDir=new File(thawDirInner,"/etc/license/")
        licenseDir.mkdirs();

        //recreate package-cache dir
        println "MKDIR PACKAGE CACHE"
        def packageCacheDir=new File(thawDirInner,"/package-cache")
        packageCacheDir.mkdirs();

        //recreate log dir
        println "MKDIR LOG"
        def logDir=new File(thawDirInner,"/log")
        logDir.mkdirs();

    }
}
