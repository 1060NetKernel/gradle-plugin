package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec

class FreezeTidyTaskSpec extends BasePluginSpec {

    def 'freezes netkernel'() {
        setup:
        File freezeDirectory = file '/test/freezeTidyTaskSpec/freeze/se'
        File installDirectory = file '/test/freezeTidyTaskSpec/install/se'

        File kernelPropertiesFile = new File(freezeDirectory, 'etc/kernel.properties')
        File netkernelShFile = new File(freezeDirectory, 'bin/netkernel.sh')

        FreezeTidyTask freezeTidyTask = createTask(FreezeTidyTask)
        freezeTidyTask.freezeDirectory = freezeDirectory
        freezeTidyTask.installDirectory = installDirectory

        String expandDirPath = 'lib/expandDir'

        File expandDir = new File(installDirectory, expandDirPath)
        kernelPropertiesFile.text = "netkernel.layer0.expandDir=${expandDir}"

        when:
        freezeTidyTask.freeze()

        then:
        !new File(freezeDirectory, "${expandDirPath}/empty.txt").exists()
        ['etc/license', 'package-cache', 'log'].each { path ->
            assert !(new File(freezeDirectory, path).exists())
        }
        kernelPropertiesFile.text.contains(expandDirPath)
        netkernelShFile.text.trim() == "INSTALLPATH='%INSTALLPATH%'"
    }

}
