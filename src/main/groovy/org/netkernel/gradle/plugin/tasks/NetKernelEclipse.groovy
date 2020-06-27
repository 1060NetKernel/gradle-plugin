package org.netkernel.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * A task to point Eclipse build target at the common gradle build for this module - to enable NK dynamic modules
 */
class NetKernelEclipse extends DefaultTask {

	@Input
	String base
	
	@Input
	String target
	
	@TaskAction
	void exec()
	{
		println("NETKERNEL ECLIPSE BUILD TARGET $target")
		def xmlFile = base+File.separator+".classpath"
		def xml = new XmlParser().parse(xmlFile)
		xml.classpathentry.each { 
		    if("output".equals(it.@kind))
		    {	it.@path = "build"+File.separator+target
		    }
		}
		new XmlNodePrinter(new PrintWriter(new FileWriter(xmlFile))).print(xml)

		//Finally remove Eclipse bin/ target if it is there
		def binFile = base+File.separator+"bin"
		new File(binFile).deleteDir()
	}
}
