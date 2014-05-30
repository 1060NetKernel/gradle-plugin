package org.netkernel.gradle.plugin.tasks

import org.netkernel.gradle.plugin.BasePluginSpec
import org.netkernel.gradle.plugin.nk.ExecutionConfig

class CleanAllTaskSpec extends BasePluginSpec {

    CleanAllTask cleanAllTask
    ExecutionConfig executionConfig

    void setup() {
        executionConfig = new ExecutionConfig('test')

        cleanAllTask = project.tasks.create(name: 'cleanAll', type: CleanAllTask)
        cleanAllTask.executionConfig = executionConfig
    }

    def 'cleans all'() {
        setup:
        File parentDirectory = file '/test/cleanAllTaskSpec'
        File deleteMeDirectory = new File(parentDirectory, 'deleteMe')
        deleteMeDirectory.mkdirs()
        executionConfig.directory = deleteMeDirectory

        when:
        cleanAllTask.cleanAll()

        then:
        !deleteMeDirectory.exists()
    }

}
