package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class InstallNetKernelTaskSpec extends BasePluginSpec {

    def 'installs netkernel successfully'() {
        setup:
        NetKernelInstance mockNetKernelInstance = Mock()

        InstallNetKernelTask installNetKernelTask = createTask(InstallNetKernelTask)
        installNetKernelTask.netKernelInstance = mockNetKernelInstance


        when:
        installNetKernelTask.installNetKernel()

        then:
        1 * mockNetKernelInstance.install()
        1 * mockNetKernelInstance.initializeModulesDir()
    }

}
