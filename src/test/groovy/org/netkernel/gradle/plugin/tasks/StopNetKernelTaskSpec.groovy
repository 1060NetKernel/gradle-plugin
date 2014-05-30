package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.nk.ExecutionConfig
import org.netkernel.gradle.plugin.util.NetKernelHelper

class StopNetKernelTaskSpec extends BasePluginSpec {

    def 'stops NetKernel'() {
        setup:
        executionConfig {
            test {}
        }

        NetKernelHelper mockNetKernelHelper = Mock()

        StopNetKernelTask stopNetKernelTask = createTask(StopNetKernelTask)
        stopNetKernelTask.configName = 'test'
        stopNetKernelTask.netKernelHelper = mockNetKernelHelper

        when:
        stopNetKernelTask.stopNetKernel()

        then:
        1 * mockNetKernelHelper.stopNetKernel(_ as ExecutionConfig)
    }

}
