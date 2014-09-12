package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/*
 * A task to synchronize and update NetKernel with latest packages from Apposite
 */

class UpdateAppositeTask extends DefaultTask {
    // Static Defaults
    static def DISTRIBUTION_URL = 'http://localhost:1060'

    //Variable parameters

    //Helpers

    @TaskAction
    void updateApposite() {
        println("UPDATING NETKERNEL WITH APPOSITE");

        def url="${DISTRIBUTION_URL}"

        def synchronize=url+"/tools/apposite/unattended/v1/synchronize";
        def update=url+"/tools/apposite/unattended/v1/update";

        callAppositeAPI(synchronize);
        sleep(5000);
        callAppositeAPI(update)
        sleep(30000);
    }

    def callAppositeAPI(url)
    {   println ("APPOSITE API CALL: $url")
        ant.get(src: url,
                dest: "temp-apposite.html",
                verbose: true,
                usetimestamp : true)
    }
    
}
