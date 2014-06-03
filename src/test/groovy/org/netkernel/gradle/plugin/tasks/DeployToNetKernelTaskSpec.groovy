package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.model.Module
import org.netkernel.gradle.plugin.model.NetKernelInstance

class DeployToNetKernelTaskSpec extends BasePluginSpec {

    def 'deploys project to NetKernel'() {
        setup:
        DeployToNetKernelTask deployToNetKernelTask = createTask(DeployToNetKernelTask)

        Module module = new Module(file('/test/DeployToNetKernelTaskSpec/module/module.xml'))
        createNetKernelExtension()
        project.netkernel.module = module

        NetKernelInstance mockNetKernelInstance = Mock()
        deployToNetKernelTask.netKernelInstance = mockNetKernelInstance

        when:
        deployToNetKernelTask.deployToNetKernel()

        then:
        1 * mockNetKernelInstance.deploy(module)
        true

    }

}
