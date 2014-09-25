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
 * A task to download a netKernelVersion of NetKernel.  This is used to download both the SE & EE Editions.
 */
class DownloadNetKernelTask extends DefaultTask {

    //Helpers
    def propertyHelper = new PropertyHelper()
    Download download

    @Input
    String netKernelVersion

    @OutputFile
    File destinationFile

    @TaskAction
    void downloadNetKernel() {
        switch (download.edition) {
            case Edition.STANDARD:
                downloadNKSEImpl(new URL(propertyHelper.findProjectProperty(project, PropertyHelper.DISTRIBUTION_URL_SE, null, [netKernelVersion: netKernelVersion])))
                break;

            case Edition.ENTERPRISE:
                def username = propertyHelper.findProjectProperty(project, "nkeeUsername", download.username)
                def password = propertyHelper.findProjectProperty(project, "nkeePassword", download.password)

                if (!username || !password) {
                    ant.fail("Downloading NetKernel Enterprise Edition requires a username and password.  Details can be found here: http://1060research.com/resources/#download")
                }

                URL url = new URL(propertyHelper.findProjectProperty(project, PropertyHelper.DISTRIBUTION_URL_EE, null, [netKernelVersion: netKernelVersion]))
                downloadNKEEImpl(url, username, password)
                break;

            default:
                ant.fail("Unknown NetKernel edition!")
                break;
        }
    }

    void downloadNKSEImpl(URL url) {
        println "Downloading ${url} to ${destinationFile}"

        ant.project.buildListeners.toList().each {
            ant.project.removeBuildListener(it)
        }

        ant.project.addBuildListener(new BuildListener() {
            void buildStarted(BuildEvent event) {}

            void buildFinished(BuildEvent event) {}

            void targetStarted(BuildEvent event) {}

            void targetFinished(BuildEvent event) {}

            void taskStarted(BuildEvent event) {}

            void taskFinished(BuildEvent event) {}

            void messageLogged(BuildEvent event) {
                DownloadNetKernelTask.this.logger.quiet event.message
            }
        })

        ant.get(src: url,
            dest: destinationFile,
            verbose: true,
            httpusecaches: true,
            usetimestamp: true)

    }

    void downloadNKEEImpl(URL url, String username, String password) {
        try {    //Prepare State Management
            HttpContext state = new BasicHttpContext();
            CookieStore cookieStore = new BasicCookieStore();
            state.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            CredentialsProvider creds = new BasicCredentialsProvider();

            //Create the client
            SchemeRegistry sreg = new SchemeRegistry();
            sreg.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
            Scheme https = new Scheme("https", 443, sf);
            sreg.register(https);
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(sreg);

            HttpParams params = new BasicHttpParams();
            DefaultHttpClient client = new DefaultHttpClient(cm, params);

            //Prepare the 1st POST request
            HttpPost post = new HttpPost("https://cs.1060research.com/csp/login");
            post.getParams().setParameter("http.protocol.expect-continue", Boolean.TRUE);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("password", password));
            formparams.add(new BasicNameValuePair("url", "https://cs.1060research.com/csp/"));
            formparams.add(new BasicNameValuePair("username", username));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(entity);

            //Make the request
            def response = client.execute(post, state)
            def statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                println("Successfully logged in to NKEE server")
                //Now we can download the distribution
                try {
                    def get = new HttpGet(url.toURI());
                    response = client.execute(get, state)
                    statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        def is = response.getEntity().getContent()
                        def fos = new FileOutputStream(destinationFile)
                        pipe(is, fos)
                        fos.flush()
                        fos.close()
                        println("Successfully downloaded ${url}")
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
                finally {    //Finally logout
                    def get = new HttpGet("https://cs.1060research.com/csp/security/logout");
                    response = client.execute(get, state)
                    println("Successfully logged out from NKEE server")
                }
            } else {
                ant.fail("Login Failed")
            }
        }
        catch (Exception e) {
            e.printStackTrace()
            ant.fail("Failed to connect to NKEE server")
        }
    }

    def public static void pipe(InputStream aInput, OutputStream aOutput) throws IOException
    {	def b = new byte[256];
        int c;
        try
        {	while ( (c=aInput.read(b))>0 )
        {	aOutput.write(b,0,c);
        }
        }
        finally
        {	try
        {	aInput.close();
        }
        finally
        {	aOutput.close();
        }
        }
    }
}
