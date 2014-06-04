package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors

@InheritConstructors
class Release implements Serializable {

    // TODO - Move to property and parameterize these values
    static final String CURRENT_MAJOR_RELEASE = "5.2.1"

    static final Map DISTRIBUTION_URL = [
        (Edition.STANDARD)  : 'http://apposite.netkernel.org/dist',
        (Edition.ENTERPRISE): 'https://cs.1060research.com/csp/download'
    ]

    static final Map RELEASE_DIR = [
        (Edition.STANDARD)  : '1060-NetKernel-SE',
        (Edition.ENTERPRISE): '1060-NetKernel-EE'
    ]

    Edition edition
    String version

    Release(Edition edition) {
        this.edition = edition
        this.version = CURRENT_MAJOR_RELEASE
    }

    URL getDownloadUrl(DownloadConfig downloadConfig) {
        // Moved from DownloadNetKernelTask
        String baseUrl = downloadConfig.url ?: DISTRIBUTION_URL[edition]
        String path = jarFileName

        if(edition == Edition.STANDARD) {
            path = "${RELEASE_DIR[edition]}/${path}"
        }

        return new URL("${baseUrl}/${path}")
    }

    String getJarFileName() {
        String filePrefix = RELEASE_DIR[edition]
        return "${filePrefix}-${version}.jar"
    }


}
