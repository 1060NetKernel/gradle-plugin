package org.netkernel.gradle.plugin.model

import org.netkernel.gradle.plugin.nk.DownloadConfig
import spock.lang.Specification

class ReleaseSpec extends Specification {

    def 'gets download url and jar file name'() {
        setup:
        Release release = new Release(edition)
        DownloadConfig downloadConfig = new DownloadConfig(url: baseUrl)

        expect:
        release.getDownloadUrl(downloadConfig).toString() == expectedUrl
        release.jarFileName == expectedJarFileName

        where:
        edition            | baseUrl          | expectedJarFileName           | expectedUrl
        Edition.STANDARD   | 'http://baseurl' | '1060-NetKernel-SE-5.2.1.jar' | 'http://baseurl/1060-NetKernel-SE/1060-NetKernel-SE-5.2.1.jar'
        Edition.STANDARD   | null             | '1060-NetKernel-SE-5.2.1.jar' | 'http://apposite.netkernel.org/dist/1060-NetKernel-SE/1060-NetKernel-SE-5.2.1.jar'
        Edition.ENTERPRISE | 'http://baseurl' | '1060-NetKernel-EE-5.2.1.jar' | 'http://baseurl/1060-NetKernel-EE-5.2.1.jar'
        Edition.ENTERPRISE | null             | '1060-NetKernel-EE-5.2.1.jar' | 'https://cs.1060research.com/csp/download/1060-NetKernel-EE-5.2.1.jar'
    }

}
