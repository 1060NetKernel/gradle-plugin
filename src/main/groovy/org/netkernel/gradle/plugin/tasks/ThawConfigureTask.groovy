package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.netkernel.gradle.plugin.util.FileSystemHelper

/**
 * Created by tab on 02/04/2014.
 */
class ThawConfigureTask extends DefaultTask {
    def FileSystemHelper fsHelper = new FileSystemHelper()
    		
    @Optional			//Hack to get around File validation on not yet created file
    def thawDirInner;

    @org.gradle.api.tasks.TaskAction
    void thaw() {

        println "CONFIGURING NETKERNEL START SCRIPT"

        //Work out if this is a windows or unix target
        def netkernelShFile=new File(thawDirInner ,"/bin/netkernel.sh");
        def windows=false
        if(!netkernelShFile.exists())
        {   netkernelShFile=new File(thawDirInner ,"/bin/netkernel.bat");
            if(!netkernelShFile.exists())
            {   throw new Exception("Can't find either netkernel.sh or netkernel.bat")
            }
            windows=true
        }
        def pr2 = new BufferedReader(new InputStreamReader(new FileInputStream(netkernelShFile),"UTF-8"));
        String line2;
        def sb2=new StringBuilder(2048);
        while ((line2=pr2.readLine())!=null)
        {   //println line2
            if (line2.startsWith("INSTALLPATH="))
            {
                def i=line2.indexOf('\'');
                if (i>0)
                {   def installPath=line2.substring(i+1);
                    line2="""INSTALLPATH='${thawDirInner}' """;
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

        //Deal with properties file
        def propertiesFile=new File(thawDirInner ,"/etc/kernel.properties");
        def expandDir=new File(thawDirInner ,"/lib/expanded/");
        pr2 = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile),"UTF-8"));
        sb2=new StringBuilder(2048);
        while ((line2=pr2.readLine())!=null)
        {   //println line2
            if (line2.startsWith("netkernel.layer0.expandDir="))
            {
                //println "FOUND EXPANDIR*********************************"
                def exp='lib/expanded/'
                if(windows)
                {   exp=exp.replaceAll("\\\\", "\\\\\\\\")        //Need to do this in the property file on windows!!!
                }
                line2="""netkernel.layer0.expandDir=${exp}""";
            }
            sb2.append(line2);
            sb2.append('\n');
        }
        pr2.close();
        fos2=new FileOutputStream(propertiesFile);
        osw2=new OutputStreamWriter(fos2,"UTF-8");
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
