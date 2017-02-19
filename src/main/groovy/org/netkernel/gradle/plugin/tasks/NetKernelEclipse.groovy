package org.netkernel.gradle.plugin.tasks

import org.apache.http.NameValuePair
import org.apache.http.client.CookieStore
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.ClientContext
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpParams
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.BuildListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.model.Download
import org.netkernel.gradle.plugin.model.Edition
import org.netkernel.gradle.plugin.model.PropertyHelper
//import org.netkernel.layer0.util.Utils

/**
 * A task to point Eclipse build target at the common gradle build for this module - to enable NK dynamic modules
 */
class NetKernelEclipse extends DefaultTask {

	String base
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
