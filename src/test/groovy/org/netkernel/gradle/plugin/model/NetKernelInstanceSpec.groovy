package org.netkernel.gradle.plugin.model

import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.conn.HttpHostConnectException
import org.netkernel.gradle.plugin.BasePluginSpec
import spock.lang.Ignore

import static org.apache.http.HttpStatus.*

class NetKernelInstanceSpec extends BasePluginSpec {

    NetKernelInstance netKernelInstance
    RESTClient mockRestClient
    HttpResponse mockHttpResponse
    StatusLine mockStatusLine

    void setup() {
        netKernelInstance = new NetKernelInstance()

        mockRestClient = Mock()
        mockHttpResponse = Mock()
        mockStatusLine = Mock()

        RESTClient.metaClass.constructor = { url -> mockRestClient }
    }

    def 'constructs NetKernelInstace'() {
        when:
        NetKernelInstance instance = new NetKernelInstance(constructorArgs)

        then:
        instance.name == "name"

        where:
        constructorArgs << ['name', [name: 'name']]
    }

    def 'is netkernel running'() {
        when:
        boolean result = netKernelInstance.isRunning()

        then:
        1 * mockRestClient.get(_ as Map) >> restClientResponse
        result == expectedResult

        where:
        restClientResponse           | expectedResult
        response(SC_OK)              | true
        response(SC_REQUEST_TIMEOUT) | false
    }

    def 'is running when exception is thrown'() {
        when:
        boolean result = netKernelInstance.isRunning()

        then:
        1 * mockRestClient.get(_ as Map) >> { throw new HttpHostConnectException(new HttpHost('localhost'), new ConnectException()) }
        result == false
    }

    def 'stops netkernel when it is running'() {
        when:
        netKernelInstance.stop()

        then:
        2 * mockRestClient.get(_ as Map) >>> [response(SC_OK), response(SC_REQUEST_TIMEOUT)]
        1 * mockRestClient.post(_ as Map) >> response(SC_OK)
    }

    def 'stops netkernel when it is not running'() {
        when:
        netKernelInstance.stop()

        then:
        1 * mockRestClient.get(_ as Map) >> response(SC_REQUEST_TIMEOUT)
    }

    @Ignore("Flush out this test")
    def 'starts netkernel'() {
        when:
        netKernelInstance.start()

        then:
        true
    }

    def 'gets instance property'() {
        when:
        String propertyValue = netKernelInstance.getInstanceProperty('netkernel:/config/netkernel.install.path')

        then:
        1 * mockRestClient.get(_ as Map) >> response
        propertyValue == expectedValue

        where:
        response                       | expectedValue
        response(SC_OK, 'installPath') | 'installPath'
        response(SC_OK, '')            | ''
        response(SC_NOT_FOUND)         | ''
    }

    def 'can deploy module'() {
        setup:
        netKernelInstance.location = file location

        when:
        boolean result = netKernelInstance.canDeployModule()

        then:
        result == expectedResult

        where:
        location                             | expectedResult
        '/test/NetKernelInstanceSpec/se'     | true
        '/test/NetKernelInstanceSpec/se.jar' | false
    }


    def 'deploys module'() {

    }

    def 'undeploys module'() {

    }

    def "doesn't install netkernel for directory instance"() {
        setup:
        netKernelInstance.location = file '/test/NetKernelInstanceSpec/se'

        when:
        netKernelInstance.install()

        then:
        thrown(Exception)
    }

    def 'installs netkernel'() {
        setup:
        netKernelInstance.location = file '/test/NetKernelInstanceSpec/se.jar'
        netKernelInstance.installationDirectory = file '/test/NetKernelInstanceSpec/installation/se'

        when:
        netKernelInstance.install()

        then:
        3 * mockRestClient.get(_ as Map) >>> [
            response(SC_OK), // Call during start() method to see if server is running
            response(SC_OK), // Call during install() to check if server is running
            response(SC_REQUEST_TIMEOUT) // Second call after installation is performed
        ]
        2 * mockRestClient.post(_ as Map) >>> [
            response(SC_OK), // Call to perform installation
            response(SC_OK) // Call to shutdown NetKernel
        ]
    }

}
