package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.Apposite
import org.netkernel.gradle.plugin.model.Deploy
import org.netkernel.gradle.plugin.model.PropertyHelper

/*
 * A task to deploy a collection of modules from Maven repository to a NetKernel instance
 * Looks like we can use Copy from Dependencies in runtime set...
 *
 * See this...
 * http://stackoverflow.com/questions/23109276/gradle-task-to-put-jars-from-maven-repository-into-project-lib-folder
 *
 * and this...
 * http://www.gradle.org/docs/current/userguide/artifact_dependencies_tutorial.html#N105A1
 *
 * Copy
 * from configurations.runtime
 * to ...
 */

class DeployCollectionTask extends Copy {
    // Static Defaults

    //Variable parameters

    //Helpers
    def propertyHelper = new PropertyHelper()
    		
    //@Input
    //Deploy  deploy
    
    def copied=[]

    def writeModulesd(modulesd, deploy)
    {
        //Set up the modules.d entry
        modulesd.mkdirs()   //First make sure target directory is there
        //Now write the modules to the xml deployment file...
        def target=new File(modulesd, "${deploy.collection}.xml")
        def s = new StringBuilder()
        s.append("<modules>\n")
        copied.each{ f ->
            //See if we need to set runlevel
            def fs=f.toString()
            def rl=null
            for(i in deploy.runlevelMap.keySet())
            {	if(fs.contains(i))
            	{	rl=deploy.runlevelMap.get(i)
            		break
            	}
            }
            if(rl!=null)
            {	s.append("""<module runlevel="$rl">./modules/$fs</module>\n""")
            }
            s.append("""<module>./modules/$fs</module>\n""")
        }
        s.append("</modules>\n")
        target.text = s
        println ("Collection deployed: ${deploy.collection}")

    }
    
}
