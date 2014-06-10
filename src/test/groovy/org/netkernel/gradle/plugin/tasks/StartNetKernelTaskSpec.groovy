package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class StartNetKernelTaskSpec extends BasePluginSpec {

    def 'start netkernel'() {
        setup:
        NetKernelInstance mockNetKernelInstance = Mock()

        StartNetKernelTask startNetKernelTask = createTask(StartNetKernelTask)
        startNetKernelTask.netKernelInstance = mockNetKernelInstance

        when:
        startNetKernelTask.start()

        then:
        1 * mockNetKernelInstance.start()
        2 * mockNetKernelInstance.isRunning() >>> [false, true]
    }

}
