package org.netkernel.gradle.plugin.tasks

import groovyx.net.http.Method
import org.gradle.api.UnknownDomainObjectException
import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.util.NetKernelHelper

class InstallNetKernelTaskSpec extends BasePluginSpec {

    InstallNetKernelTask installNetKernelTask
    NetKernelHelper mockNetKernelHelper

    void setup() {
        mockNetKernelHelper = Mock()

        installNetKernelTask = createTask(InstallNetKernelTask)
        installNetKernelTask.configName = 'test'
        installNetKernelTask.nkHelper = mockNetKernelHelper

        executionConfig {
            test {
                directory = file '/test/installNetKernelTaskSpec'
            }
        }
    }

    def 'installs netkernel successfully'() {
        when:
        installNetKernelTask.installNK()

        then:
        2 * mockNetKernelHelper.isNetKernelRunning() >>> [true, false]
        2 * mockNetKernelHelper.issueRequest(NetKernelHelper.BEF, Method.POST, _ as Map) >> true
    }

    def 'throws exception if no config is found'() {
        setup:
        installNetKernelTask.configName = 'notfound'

        when:
        installNetKernelTask.installNK()

        then:
        thrown(UnknownDomainObjectException)
    }

}
