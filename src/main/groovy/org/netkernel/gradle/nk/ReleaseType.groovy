package org.netkernel.gradle.nk

/**
 * An enum of NetKernel releases.
 */

enum ReleaseType {

    NKSE("SE"),
    NKEE("EE")

    private String name

    def static CURRENT_MAJOR_RELEASE = "5.2.1"

    private ReleaseType() {
    }

    ReleaseType(String name) {
        this.name = name
    }

    public String toString() {
        name
    }
}
