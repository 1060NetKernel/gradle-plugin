package org.netkernel.gradle.plugin

/**
 * The details about a desired NetKernel execution.
 */
class ExecutionConfig {
    enum Mode {
        NETKERNEL_FULL,
        NETKERNEL_INSTALL,
        NETKERNEL_EMBEDDED
    }

    def release
    def directory
    def installJar
    def mode
}
