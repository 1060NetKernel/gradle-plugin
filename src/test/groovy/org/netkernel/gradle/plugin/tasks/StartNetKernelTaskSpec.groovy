package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.nk.ExecutionConfig
import org.netkernel.gradle.plugin.util.NetKernelHelper

class StartNetKernelTaskSpec extends BasePluginSpec {

    def 'start netkernel'() {
        setup:
        ExecutionConfig config = new ExecutionConfig('name')
        config.directory = 'directory'

        String configName = "config"
        project.netkernel = [envs: [(configName): config]]

        StartNetKernelTask startNetKernelTask = createTask(StartNetKernelTask)

        NetKernelHelper mockNetKernelHelper = Mock()
        startNetKernelTask.nkHelper = mockNetKernelHelper
        startNetKernelTask.configName = configName

        when:
        startNetKernelTask.start()

        then:
        1 * mockNetKernelHelper.startNetKernel(_ as ExecutionConfig)
        2 * mockNetKernelHelper.isNetKernelRunning() >>> [false, true]
    }

}
