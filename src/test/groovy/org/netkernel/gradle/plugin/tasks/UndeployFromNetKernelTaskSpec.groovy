package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class UndeployFromNetKernelTaskSpec extends BasePluginSpec {

    def 'undeploys module'() {
        setup:
        NetKernelInstance mockNetKernelInstance = Mock()
        File moduleArchiveFile = file '/test/UndeployFromNetKernelTaskSpec/module.jar'

        UndeployFromNetKernelTask undeployFromNetKernelTask = createTask(UndeployFromNetKernelTask)
        undeployFromNetKernelTask.moduleArchiveFile = moduleArchiveFile
        undeployFromNetKernelTask.netKernelInstance = mockNetKernelInstance

        when:
        undeployFromNetKernelTask.undeploy()

        then:
        1 * mockNetKernelInstance.undeploy(moduleArchiveFile)
    }
}
