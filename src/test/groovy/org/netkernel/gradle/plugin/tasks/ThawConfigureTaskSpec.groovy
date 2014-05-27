package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.util.FileSystemHelper

class ThawConfigureTaskSpec extends BasePluginSpec {

    def 'thaws configuration'() {
        setup:
        File thawDirInner = new File(ThawConfigureTaskSpec.getResource('/test/thawConfigureTaskSpec').file)

        ThawConfigureTask thawConfigureTask = createTask(ThawConfigureTask)

        FileSystemHelper mockFileSystemHelper = Mock()
        thawConfigureTask.fsHelper = mockFileSystemHelper
        thawConfigureTask.thawDirInner = thawDirInner.absolutePath

        when:
        thawConfigureTask.thaw()

        then:
        ['/bin/netkernel.sh', '/etc/license', '/package-cache', '/log'].each { dir ->
            assert new File(thawDirInner, dir).exists()
        }
        new File(thawDirInner, '/bin/netkernel.sh').text.contains(thawDirInner.absolutePath)
    }

}
