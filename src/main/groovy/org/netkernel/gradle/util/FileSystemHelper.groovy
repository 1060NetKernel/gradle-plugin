package org.netkernel.gradle.util

/**
 * A helper class for interacting with the file systems.
 */
class FileSystemHelper {
    /**
     * Check whether the specified directory exists or not.
     * @param dir
     * @return
     */
    boolean dirExists(String dir) {
        new File(dir).exists()
    }

    /**
     * Get the Gradle Home directory for this user.
     * @return the current user's Gradle home dir
     */
    def gradleHomeDir() {
        return "${System.properties['user.home']}/.gradle"
    }

    /**
     * Create the specified directory
     * @param dirName
     * @return
     */
    def createDirectory(String dirName) {
        boolean retValue = false

        def f = new File(dirName)
        retValue = f.isDirectory() && f.exists()

        if(!retValue) {
            retValue = f.mkdirs()
        }

        return retValue
    }

    /**
     * Retrieve the name of the specified directory in relation
     * to the user's Gradle home directory.
     * @param dirName
     * @return
     */
    def dirInGradleHomeDirectory(String dirName) {
        "${gradleHomeDir()}/$dirName"
    }

    def dirExistsInGradleHomeDirectory(String dirName) {
        return existsDir(dirInGradleHomeDirectory(dirName))
    }

    /**
     * Create the specified directory in the user's Gradle Home directory.
     * @param dirName
     * @return
     */
    def createDirInGradleHomeDirectory(String dirName) {
        def dir = "${gradleHomeDir}/$dirName"
        return createDirectory(dir)
    }
}
