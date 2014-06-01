package org.netkernel.gradle.plugin.util

import spock.lang.Specification

class FileSystemHelperSpec extends Specification {

    FileSystemHelper fileSystemHelper = new FileSystemHelper()

    def 'dir exists #directory'() {
        setup:
        File file = new File(FileSystemHelperSpec.getResource('/test').file, path)

        when:
        boolean result = fileSystemHelper.exists(file.absolutePath)

        then:
        result == expectedResult

        when:
        result = fileSystemHelper.exists(file)

        then:
        result == expectedResult

        where:
        path                             | expectedResult
        '/gradleHomeDirectory'           | true
        '/gradleHomeDirectory/netkernel' | true
        '/doesntexist'                   | false
    }

    def 'gets gradle home dir'() {
        expect:
        fileSystemHelper.gradleHomeDir().absolutePath.endsWith('/.gradle')
    }

    def 'creates directories'() {
        setup:
        File dirName = new File(FileSystemHelperSpec.getResource('/test').file, directory)

        when:
        boolean result = fileSystemHelper.createDirectory(dirName.absolutePath)

        then:
        result == expectedResult

        where:
        directory            | expectedResult
        'workdir'            | true
        'workdir/newfolder'  | true
        'workdir/readme.txt' | false
    }

    def 'retrieves directory in gradle home directory'() {
        setup:
        File gradleHome = new File(FileSystemHelperSpec.getResource('/test/gradleHomeDirectory').file)
        fileSystemHelper.@_gradleHome = gradleHome

        expect:
        fileSystemHelper.fileInGradleHome('netkernel') == new File(gradleHome, 'netkernel')
    }

    def 'directory exists in gradle home directory'() {
        setup:
        File gradleHome = new File(FileSystemHelperSpec.getResource('/test/gradleHomeDirectory').file)
        fileSystemHelper.@_gradleHome = gradleHome

        expect:
        fileSystemHelper.dirExistsInGradleHomeDirectory('netkernel')
    }

    def 'creates directory in gradle home directory'() {
        setup:
        File gradleHome = new File(FileSystemHelperSpec.getResource('/test/gradleHomeDirectory').file)
        fileSystemHelper.@_gradleHome = gradleHome

        when:
        fileSystemHelper.createDirInGradleHomeDirectory('hello')

        then:
        gradleHome.listFiles().find { it.name == 'hello' }
    }

}
