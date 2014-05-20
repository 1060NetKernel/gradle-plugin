package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.util.FileSystemHelper

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
        File tempFolder = new File(CleanAllTaskSpec.getResource('/test/cleanAllTempFolder').file)

        when:
        cleanAllTask.cleanAll()

        then:
        1 * mockFileSystemHelper.dirInGradleHomeDirectory('netkernel') >> tempFolder.absolutePath
        !tempFolder.exists()
    }

}
