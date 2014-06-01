package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec

class FreezeTidyTaskSpec extends BasePluginSpec {

    def 'freezes netkernel'() {
        setup:
        File freezeDirInner = getResourceAsFile('/test/freezeTidyTaskSpec/freezeDirInner')
        File kernelPropertiesFile = new File(freezeDirInner, 'etc/kernel.properties')
        File netkernelShFile = new File(freezeDirInner, 'bin/netkernel.sh')
        File expandDir = new File(getResource('/test/freezeTidyTaskSpec/expandDir').file)

        FreezeTidyTask freezeTidyTask = createTask(FreezeTidyTask)
        freezeTidyTask.freezeDirInner = freezeDirInner.absolutePath
        freezeTidyTask.installDirInner = getResource('/test/freezeTidyTaskSpec/installDirInner').file

        kernelPropertiesFile.text = "netkernel.layer0.expandDir=${expandDir}"

        when:
        freezeTidyTask.freeze()

        then:
        !new File(expandDir, 'empty.txt').exists()
        ['etc/license', 'package-cache', 'log'].each { path ->
            assert !(new File(freezeDirInner, path).exists())
        }
        kernelPropertiesFile.text.contains(expandDir.absolutePath)
        netkernelShFile.text.trim() == "INSTALLPATH='%INSTALLPATH%'"
    }

}
