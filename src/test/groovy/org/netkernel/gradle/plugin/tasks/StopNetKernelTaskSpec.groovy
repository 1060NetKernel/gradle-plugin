package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class StopNetKernelTaskSpec extends BasePluginSpec {

    def 'stops NetKernel'() {
        setup:
        NetKernelInstance mockNetKernelInstance = Mock()

        StopNetKernelTask stopNetKernelTask = createTask(StopNetKernelTask)
        stopNetKernelTask.netKernelInstance = mockNetKernelInstance

        when:
        stopNetKernelTask.stopNetKernel()

        then:
        1 * mockNetKernelInstance.stop()
    }

}
