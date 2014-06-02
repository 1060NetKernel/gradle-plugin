package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors

@InheritConstructors
class Release {

    // TODO - Move to property
    static final String CURRENT_MAJOR_RELEASE = "5.2.1"

    Edition edition
    String version

    Release(Edition edition) {
        this.edition = edition
        this.version = CURRENT_MAJOR_RELEASE
    }



}
