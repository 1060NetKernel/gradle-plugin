package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import spock.lang.Ignore

class CleanAllTaskSpec extends BasePluginSpec {

    CleanAllTask cleanAllTask

    void setup() {
        cleanAllTask = createTask(CleanAllTask)
    }

    @Ignore
    def 'cleans all'() {
        setup:
        File parentDirectory = file '/test/cleanAllTaskSpec'
        File deleteMeDirectory = new File(parentDirectory, 'deleteMe')
        deleteMeDirectory.mkdirs()

        when:
        cleanAllTask.cleanAll()

        then:
        !deleteMeDirectory.exists()
    }

}
