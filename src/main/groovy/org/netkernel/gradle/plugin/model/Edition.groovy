package org.netkernel.gradle.plugin.model

/**
 * An enum of NetKernel releases.
 */
enum Edition {

    STANDARD("SE"),
    ENTERPRISE("EE")

    // TODO - Move to property
    static final String CURRENT_MAJOR_RELEASE = "5.2.1"

    private String name

    private Edition(String name) {
        this.name = name
    }

    String toString() {
        name
    }
}
