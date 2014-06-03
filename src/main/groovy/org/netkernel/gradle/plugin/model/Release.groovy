package org.netkernel.gradle.plugin.model

import groovy.transform.InheritConstructors
import org.netkernel.gradle.plugin.nk.DownloadConfig

@InheritConstructors
class Release implements Serializable {

    //    //Variable parameters
//    //TODO: Drive some of this from the ExecutionConfigs?
////    String release = NKSE
////    String version = '5.2.1' //TODO Needs to be parameterised
//    String baseURL = DISTRIBUTION_URL
//    String releaseDir
//    String filePrefix

    //    // Static Defaults
//    // TODO - Push to properties files
//    static def DISTRIBUTION_URL = 'http://apposite.netkernel.org/dist'
////    static def NKSE = 'SE'
////    static def NKEE = 'EE'
//    static def DEFAULT_RELEASEDIRS = ['SE': '1060-NetKernel-SE',
//                                      'EE': '1060-NetKernel-EE']

    // TODO - Move to property
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
