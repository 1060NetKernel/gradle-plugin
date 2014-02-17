package org.netkernel.gradle.plugin

import org.netkernel.gradle.nk.ReleaseType

/**
 * The details about a desired NetKernel execution.
 */
class ExecutionConfig {
    enum Mode {
        NETKERNEL_FULL,
        NETKERNEL_INSTALL,
        NETKERNEL_EMBEDDED
    }

    enum Type {
        NKSE,
        NKEE
    }

    def name
    def url
    def port
    def release
    def directory
    def installJar
    def mode
    def supportsDaemonModules
    ReleaseType relType

    ExecutionConfig(String name) {
        this.name = name
    }
}
