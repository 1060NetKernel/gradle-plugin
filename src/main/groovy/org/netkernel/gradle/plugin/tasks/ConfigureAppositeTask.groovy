package org.netkernel.gradle.plugin.tasks

import org.gradle.api.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to configure NetKernel by installing packages from Apposite
 */

class ConfigureAppositeTask extends DefaultTask {
    // Static Defaults
    //static def DISTRIBUTION_URL = 'http://localhost:1060'


    //Variable parameters
    def packageList=[]

    //Helpers
    def propertyHelper = new PropertyHelper()

    @TaskAction
    void configureApposite() {
        println("CONFIGURING APPOSITE: ");

        def url=propertyHelper.findProjectProperty(project, propertyHelper.NETKERNEL_INSTANCE_DEFAULT_URL, "http://localhost")+
                ":"+
                propertyHelper.findProjectProperty(project, propertyHelper.NETKERNEL_INSTANCE_BACKEND_PORT, "1060")

        def install=url+"/tools/apposite/unattended/v1/change?";
        packageList.each { p ->
            install+="install=${p}&"
            println(p)
        }

        callAppositeAPI(install)
        sleep(20000)
    }

    def callAppositeAPI(url)
    {   println ("APPOSITE API CALL: $url")
        ant.get(src: url,
                dest: "temp-apposite.html",
                verbose: true,
                usetimestamp : true)
    }
    
}
