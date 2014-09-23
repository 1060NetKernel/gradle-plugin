package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.Apposite
import org.netkernel.gradle.plugin.model.Deploy
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to deploy a collection of modules from Maven repository to a NetKernel instance
 */

class DeployCollectionTask extends DefaultTask {
    // Static Defaults

    //Variable parameters

    //Helpers
    def propertyHelper = new PropertyHelper()
    Deploy  deploy

    @TaskAction
    void deployCollection() {

        println ("Collection to be deployed: ${deploy.collection}")
        deploy.modules.each{ m ->
            println(""" ${m["group"]} : ${m["name"]} : ${m["version"]} """)
        }

    }


    
}
