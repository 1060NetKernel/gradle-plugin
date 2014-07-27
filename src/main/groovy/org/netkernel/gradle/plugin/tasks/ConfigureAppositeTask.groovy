package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

//Imports for Apache Client used in NKEE download
/*
 * A task to download a netKernelVersion of NetKernel.
 */

class ConfigureAppositeTask extends DefaultTask {
    // Static Defaults
    static def DISTRIBUTION_URL = 'http://localhost:1060'


    //Variable parameters

    //Helpers

    @TaskAction
    void configureApposite() {
        println("CONFIGURING APPOSITE");

        def url="${DISTRIBUTION_URL}"

        synchronize=url+"/tools/apposite/unattended/v1/synchronize";
        update=url+"/tools/apposite/unattended/v1/update";
        install=url+"/tools/apposite/unattended/v1/change?install=lang-trl&install=html5-frameworks";

        callAppositeAPI(synchronize);
        sleep(5000);
        callAppositeAPI(update)
        sleep(30000);
        callAppositeAPI(install)
    }

    def callAppositeAPI(url)
    {   println ("APPOSITE API CALL: $url")
        ant.get(src: url,
                dest: "temp-apposite.html",
                verbose: true,
                usetimestamp : true)
    }
    
}
