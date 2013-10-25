package org.netkernel.gradle.util

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: brian
 * Date: 10/21/13
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
class NetKernelHelperTest {
    @Test
    def void testRunning() {
        NetKernelHelper nkHelper = new NetKernelHelper()
        println "Is NetKernel running: " + nkHelper.isNetKernelRunning()
    }
}
