package org.netkernel.gradle.plugin.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by randolph.kahle on 3/31/14.
 */
@Slf4j
class CreateAppositePackageTask extends DefaultTask {

    @TaskAction
    void createAppositePackage() {
        log.info "Creating apposite package"
    }
}
