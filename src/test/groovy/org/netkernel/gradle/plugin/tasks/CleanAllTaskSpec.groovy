package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.util.FileSystemHelper

class CleanAllTaskSpec extends BasePluginSpec {

    CleanAllTask cleanAllTask
    FileSystemHelper mockFileSystemHelper

    void setup() {
        mockFileSystemHelper = Mock()

        cleanAllTask = project.tasks.create(name: 'cleanAll', type: CleanAllTask)
        cleanAllTask.fsHelper = mockFileSystemHelper
    }

    def 'cleans all'() {
        setup:
        File netKernelDirectory = new File(CleanAllTaskSpec.getResource('/test/workdir').file, 'deleteme')

        when:
        boolean result = netKernelDirectory.mkdirs()

        then:
        result == true

        when:
        cleanAllTask.cleanAll()

        then:
        1 * mockFileSystemHelper.dirInGradleHomeDirectory('netkernel') >> netKernelDirectory
        !netKernelDirectory.exists()
    }

}
