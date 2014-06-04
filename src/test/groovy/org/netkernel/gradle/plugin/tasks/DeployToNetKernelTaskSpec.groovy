package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.NetKernelInstance

class DeployToNetKernelTaskSpec extends BasePluginSpec {

    def 'deploys project to NetKernel'() {
        setup:
        DeployToNetKernelTask deployToNetKernelTask = createTask(DeployToNetKernelTask)

        NetKernelInstance mockNetKernelInstance = Mock()
        deployToNetKernelTask.netKernelInstance = mockNetKernelInstance

        File moduleArchiveFile = file '/test/DeployToNetKernelTaskSpec/module.jar'
        deployToNetKernelTask.moduleArchiveFile = moduleArchiveFile

        when:
        deployToNetKernelTask.deployToNetKernel()

        then:
        1 * mockNetKernelInstance.deploy(moduleArchiveFile)
    }

}
