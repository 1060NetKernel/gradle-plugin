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
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.nk.DownloadConfig
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.PropertyHelper
import org.netkernel.layer0.util.Utils

//Imports for Apache Client used in NKEE download
/*
 * A task to download a version of NetKernel.
 */

class ConfigureApposite extends DefaultTask {
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
