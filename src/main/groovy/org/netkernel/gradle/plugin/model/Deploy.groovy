package org.netkernel.gradle.plugin.model

import org.gradle.api.Project

/**
 *  A simple class to manage deploy collection configuration.
 */
class Deploy implements Serializable{
    def collection
    def runlevelMap=[:]

    final Project project

    Deploy(Project project) {
        this.project = project
    }

    def module (moduleMap)
    {   
    	def tempMap=[group: moduleMap['group'], name: moduleMap['name'], version: moduleMap['version']]
    	project.dependencies.nkdeploy tempMap
    	//Check for runlevel
    	def level=moduleMap.get('runlevel')
    	if(level!=null)
    	{	runlevelMap.put(moduleMap['name'], level)    		
    	}
    }
}
