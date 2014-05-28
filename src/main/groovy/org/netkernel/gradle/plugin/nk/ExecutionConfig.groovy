package org.netkernel.gradle.plugin.nk
/**
 * The details about a desired NetKernel execution.
 */
class ExecutionConfig {
    enum Mode {
        NETKERNEL_FULL,
        NETKERNEL_INSTALL,
        NETKERNEL_EMBEDDED
    }

    def name
    def url
    def frontEndPort
    def backEndPort
    def release
    File directory
    def installJar
    def mode
    def supportsDaemonModules
    ReleaseType relType

    ExecutionConfig(String name) {
        this.name = name
        this.relType = ReleaseType.NKSE
        this.mode = Mode.NETKERNEL_FULL
        this.url = "http://localhost"
        this.frontEndPort = 8080
        this.backEndPort = 1060
        this.release = ReleaseType.CURRENT_MAJOR_RELEASE
    }
}
