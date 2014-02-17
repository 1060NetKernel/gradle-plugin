package org.netkernel.gradle.nk

/**
 * An enum of NetKernel releases.
 */

enum ReleaseType {
    NKSE("SE"),
    NKEE("EE")

    private String name

    private ReleaseType() {
    }

    ReleaseType(String name) {
        this.name = name
    }

    public String toString() {
        return name
    }
}
