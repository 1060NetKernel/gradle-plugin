package org.netkernel.gradle.plugin.util

import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.nk.ExecutionConfig
import spock.lang.Ignore

class NetKernelHelperSpec extends BasePluginSpec {

    NetKernelHelper netKernelHelper

    RESTClient mockRestClient
    HttpResponse mockHttpResponse
    StatusLine mockStatusLine

    void setup() {
        mockRestClient = Mock()
        mockHttpResponse = Mock()
        mockStatusLine = Mock()

        netKernelHelper = new NetKernelHelper()

        // Groovy is sweet, how about this one...
        RESTClient.metaClass.constructor = { url -> mockRestClient }
    }

    def 'is netkernel running'() {
        when:
        boolean result = netKernelHelper.isNetKernelRunning()

        then:
        1 * mockRestClient.get(_ as Map) >> mockHttpResponse
        1 * mockHttpResponse.statusLine >> mockStatusLine
        1 * mockStatusLine.statusCode >> HttpStatus.SC_REQUEST_TIMEOUT
        result == false
    }

    def 'is netkernel installed'() {
        when:
        boolean result = netKernelHelper.isNetKernelInstalled()

        then:
        result == false
    }

    @Ignore('Ignore for now until we get the ProcessBuilder stuff mocked out')
    def 'starts netkernel'() {
        setup:
        ExecutionConfig executionConfig = new ExecutionConfig('name')
        executionConfig.mode = ExecutionConfig.Mode.NETKERNEL_INSTALL

        when:
        netKernelHelper.startNetKernel(executionConfig)

        then:
        1 * mockRestClient.get(_ as Map) >> mockHttpResponse
        1 * mockHttpResponse.statusLine >> mockStatusLine
        1 * mockStatusLine.statusCode >> HttpStatus.SC_OK

        netKernelHelper.isNetKernelRunning() == true
    }

    def 'where is netkernel installed'() {
        when:
        String result = netKernelHelper.whereIsNetKernelInstalled()

        then:
        result != null
    }
}
