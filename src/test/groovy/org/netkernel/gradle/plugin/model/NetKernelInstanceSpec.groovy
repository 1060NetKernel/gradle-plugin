package org.netkernel.gradle.plugin.model

import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.netkernel.gradle.plugin.BasePluginSpec
import spock.lang.Ignore

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

    def 'is running'() {
        when:
        boolean result = netKernelInstance.isRunning()

        then:
        1 * mockRestClient.get(_ as Map) >> mockHttpResponse
        1 * mockHttpResponse.statusLine >> mockStatusLine
        1 * mockStatusLine.statusCode >> HttpStatus.SC_REQUEST_TIMEOUT
        result == false
    }

    def 'stops netkernel when it is running'() {
        when:
        netKernelInstance.stop()

        then:
        2 * mockRestClient.get(_ as Map) >>> [response(HttpStatus.SC_OK), response(HttpStatus.SC_REQUEST_TIMEOUT)]
        1 * mockRestClient.post(_ as Map) >> response(HttpStatus.SC_OK)
    }

    def 'stops netkernel when it is not running'() {
        when:
        netKernelInstance.stop()

        then:
        1 * mockRestClient.get(_ as Map) >> response(HttpStatus.SC_REQUEST_TIMEOUT)
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
        response                                  | expectedValue
        response(HttpStatus.SC_OK, 'installPath') | 'installPath'
        response(HttpStatus.SC_OK, '')            | ''
        response(HttpStatus.SC_NOT_FOUND)         | ''
    }

//    def 'deploy module '

}
