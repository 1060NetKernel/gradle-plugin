package org.netkernel.gradle.util

/**
 * A helper class for interacting with a NetKernel instance.
 */
class NetKernelHelper {
    def issueRequest(Closure request) {
        println "RUNNING: " + request
    }

    def isNetKernelRunning() {
       issueRequest {
           path : '/'
       }
    }
}
