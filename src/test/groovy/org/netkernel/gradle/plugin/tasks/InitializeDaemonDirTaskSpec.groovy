package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.nk.ExecutionConfig

class InitializeDaemonDirTaskSpec extends BasePluginSpec {

    def 'check installation'() {
        setup:
        File baseTestDirectory = getResourceAsFile('/test/initializeDaemonDirTaskSpec')

        ExecutionConfig config = new ExecutionConfig('name')
        config.directory = baseTestDirectory.absoluteFile
        project.netkernel = [envs: ['name': config]]

        InitializeDaemonDirTask initializeDaemonDirTask = createTask(InitializeDaemonDirTask)
        initializeDaemonDirTask.configName = 'name'

        when:
        initializeDaemonDirTask.checkInstallation()

        then:
        new File(baseTestDirectory, 'etc/kernel.properties').text == 'netkernel.init.modulesdir=etc/modules.d/'
    }

}
