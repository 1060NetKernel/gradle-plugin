package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to synchronize and update NetKernel with latest packages from Apposite
 */

class UpdateAppositeTask extends DefaultTask {
    // Static Defaults

    //Variable parameters

    //Helpers
    def propertyHelper = new PropertyHelper()

    @Input
    NetKernelInstance netKernelInstance

    @TaskAction
    void updateApposite() {
        println("UPDATING NETKERNEL WITH APPOSITE");


        def url=netKernelInstance.getUrl().toString()+
                ":"+
                netKernelInstance.getBackendPort().toString()

        def synchronize=url+"/tools/apposite/unattended/v1/synchronize";
        def update=url+"/tools/apposite/unattended/v1/update";

        sleep(30000);	//Allow time in case we have just booted for first time and automatic apposite update is running
        callAppositeAPI(synchronize);
        sleep(15000);
        callAppositeAPI(update)
        sleep(90000);
    }

    def callAppositeAPI(url)
    {   println ("APPOSITE API CALL: $url")
        ant.get(src: url,
                dest: "temp-apposite.html",
                verbose: true,
                usetimestamp : true)
    }
    
}
