package org.netkernel.gradle.plugin.model

import org.netkernel.gradle.plugin.BasePluginSpec

class NetKernelInstanceSpec extends BasePluginSpec {

    def "deploys module file to netkernel instance when modules.d folder doesn't exist"() {
        setup:
        File location = file("/test/NetKernelInstanceSpec/se")
        NetKernelInstance netKernelInstance = new NetKernelInstance(location: location)
        File moduleArchiveFile = file("/test/NetKernelInstanceSpec/urn.org.netkernel.gradle.testmodule/build/urn.org.netkernel.gradle.testmodule.jar")

        when:
        netKernelInstance.deploy(moduleArchiveFile)

        then:
        new File(location, "etc/modules.d/urn.org.netkernel.gradle.testmodule.xml").exists()
    }

}
