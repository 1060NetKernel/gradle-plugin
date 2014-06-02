package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import spock.lang.Ignore

class InitializeDaemonDirTaskSpec extends BasePluginSpec {

    @Ignore('TODO - Implement InitializeDaemonDirTask and run tests')
    def 'check installation'() {
        setup:
        File baseTestDirectory = getResourceAsFile('/test/initializeDaemonDirTaskSpec')

        InitializeDaemonDirTask initializeDaemonDirTask = createTask(InitializeDaemonDirTask)
        initializeDaemonDirTask.configName = 'name'

        when:
        initializeDaemonDirTask.checkInstallation()

        then:
        new File(baseTestDirectory, 'etc/kernel.properties').text == 'netkernel.init.modulesdir=etc/modules.d/'
    }

}
