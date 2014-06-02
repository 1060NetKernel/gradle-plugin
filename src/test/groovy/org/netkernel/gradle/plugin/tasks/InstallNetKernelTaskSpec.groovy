package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class InstallNetKernelTaskSpec extends BasePluginSpec {

    InstallNetKernelTask installNetKernelTask
    NetKernelInstance mockNetKernelInstance

    void setup() {
        mockNetKernelInstance = Mock()

        installNetKernelTask = createTask(InstallNetKernelTask)
        installNetKernelTask.netKernelInstance = mockNetKernelInstance
    }

    def 'installs netkernel successfully'() {
        when:
        installNetKernelTask.installNK()

        then:
        1 * mockNetKernelInstance.install()
    }

}
