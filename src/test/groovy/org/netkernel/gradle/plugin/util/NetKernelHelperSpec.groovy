package org.netkernel.gradle.plugin.util

import org.netkernel.gradle.plugin.BasePluginSpec

class NetKernelHelperSpec extends BasePluginSpec {

    NetKernelHelper netKernelHelper

    void setup() {
        netKernelHelper = new NetKernelHelper()
    }

//    def 'is netkernel running'() {
//        when:
//        boolean result = netKernelHelper.isNetKernelRunning()
//
//        then:
//        result == false
//    }
//
////    def 'is netkernel installed'() {
////        when:
////        boolean result = netKernelHelper.isNetKernelInstalled()
////
////        then:
////        result == false
////    }
//
//
//    def 'starts netkernel'() {
//        setup:
//        ExecutionConfig executionConfig = new ExecutionConfig('name')
//        executionConfig.mode = ExecutionConfig.Mode.NETKERNEL_INSTALL
//
//        when:
//        netKernelHelper.startNetKernel(executionConfig)
//
//        then:
//        netKernelHelper.isNetKernelRunning() == true
//    }
//
//    def 'where is netkernel installed'() {
//        when:
//        String result = netKernelHelper.whereIsNetKernelInstalled()
//
//        then:
//        result != null
//    }
}
