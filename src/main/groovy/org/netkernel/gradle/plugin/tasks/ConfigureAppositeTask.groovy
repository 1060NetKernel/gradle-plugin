package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/*
 * A task to configure NetKernel by installing packages from Apposite
 */

class ConfigureAppositeTask extends DefaultTask {
    // Static Defaults
    static def DISTRIBUTION_URL = 'http://localhost:1060'

    //Variable parameters
    def packageList=[]

    //Helpers

    @TaskAction
    void configureApposite() {
        println("CONFIGURING APPOSITE: ");

        def url="${DISTRIBUTION_URL}"

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
