package org.netkernel.gradle.plugin.tasks
import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.BuildListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.netkernel.gradle.plugin.DownloadConfig
import org.netkernel.gradle.util.FileSystemHelper
import org.netkernel.gradle.util.PropertyHelper
import org.netkernel.layer0.util.Utils

//Imports for Apache Client used in NKEE download
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;


/*
 * A task to download a version of NetKernel.
 */
class DownloadNetKernelTask extends DefaultTask {
    // Static Defaults
    static def DISTRIBUTION_URL = 'http://apposite.netkernel.org/dist'
    static def NKSE = 'SE'
    static def NKEE = 'EE'
    static def DEFAULT_RELEASEDIRS = [ 'SE' : '1060-NetKernel-SE',
                                       'EE' : '1060-NetKernel-EE' ]

    DownloadConfig downloadConfig

    //Variable parameters
    //TODO: Drive some of this from the ExecutionConfigs?
    String release = NKSE
    String version = '5.2.1' //TODO Needs to be parameterised
    String baseURL = DISTRIBUTION_URL
    String releaseDir
    String filePrefix

    //Helpers
    def fsHelper = new FileSystemHelper()
    def propHelper = new PropertyHelper()

    @TaskAction
    void downloadNetKernel() {
        def dest = fsHelper.dirInGradleHomeDirectory("netkernel/download")
        if(!fsHelper.dirExists(dest)&&!fsHelper.createDirectory(dest)) {
            ant.fail("Error creating: ${dest}")
        }

        //Set base parameters
        if(downloadConfig.url != null) {
            baseURL = downloadConfig.url
        }
        if(releaseDir == null) {
            releaseDir = DEFAULT_RELEASEDIRS[release]
        }
        if(filePrefix == null) {
            filePrefix = DEFAULT_RELEASEDIRS[release]
        }

        switch(release) {
            case NKSE:
                downloadNKSEImpl("${baseURL}/${releaseDir}/${filePrefix}-${version}.jar", dest)
            break;
            case NKEE:
                def username = propHelper.findProjectProperty(project, "nkeeUsername", 
                    downloadConfig.username)
                def password = propHelper.findProjectProperty(project, "nkeePassword", 
                    downloadConfig.password)
                    
                if(!username || !password) {
                    ant.fail("Downloading NKEE requires a username and password")
                }
                
                downloadNKEEImpl("${filePrefix}-${version}.jar", dest, username, password)
            break;
            default:
            	ant.fail("Unknown NetKernel version!")
            break;
        }
    }
    
    void downloadNKSEImpl(url, dest)
    {	println "Downloading ${url} to ${dest}"

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
                dest: dest,
                verbose: true,
                httpusecaches : true,
                usetimestamp : true)
    	
    }
    
    void downloadNKEEImpl(distribution, dest, username, password)
    {
    	try
        {	//Prepare State Management
        	HttpContext state=new BasicHttpContext();
        	CookieStore cookieStore = new BasicCookieStore();
        	state.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        	CredentialsProvider creds=new BasicCredentialsProvider();
			
			//Create the client
			SchemeRegistry sreg=new SchemeRegistry();
			sreg.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			SSLSocketFactory sf=SSLSocketFactory.getSocketFactory();
			Scheme https=new Scheme("https", 443, sf);
			sreg.register(https);
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(sreg);

			HttpParams params = new BasicHttpParams();
			DefaultHttpClient client=new DefaultHttpClient(cm, params);
			
			//Prepare the 1st POST request
			HttpPost post=new HttpPost("https://cs.1060research.com/csp/login");
			post.getParams().setParameter("http.protocol.expect-continue", Boolean.TRUE);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("password", password));
			formparams.add(new BasicNameValuePair("url", "https://cs.1060research.com/csp/"));
			formparams.add(new BasicNameValuePair("username", username));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.setEntity(entity);

			//Make the request
			def response=client.execute(post, state)
			def statusCode=response.getStatusLine().getStatusCode();
			if(statusCode==200)
			{	println("Successfully logged in to NKEE server")
				//Now we can download the distribution
				try
				{
					def get=new HttpGet("https://cs.1060research.com/csp/download/${distribution}");
					response=client.execute(get, state)
					statusCode=response.getStatusLine().getStatusCode();
					if(statusCode==200)
					{	def is=response.getEntity().getContent()
						def f=new File(dest, distribution)
						def fos=new FileOutputStream(f)
						Utils.pipe(is, fos)
						fos.flush()
						fos.close()
						println("Successfully downloaded ${distribution}")
					}
				}
				catch(Exception e)
				{	e.printStackTrace()
				}
				finally
				{	//Finally logout
					def get=new HttpGet("https://cs.1060research.com/csp/security/logout");
					response=client.execute(get, state)
					println("Successfully logged out from NKEE server")
				}
			}
			else
			{	ant.fail("Login Failed")				
			}
        }
        catch(Exception e)
        {	e.printStackTrace()
        	ant.fail("Failed to connect to NKEE server")
        }
    }
}
