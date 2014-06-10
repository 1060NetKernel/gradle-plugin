package org.netkernel.gradle.plugin.model

/**
 * An enum of NetKernel editions.
 */
enum Edition {

    STANDARD("SE"),
    ENTERPRISE("EE")

    private String name

    private Edition(String name) {
        this.name = name
    }

    String toString() {
        name
    }
}
