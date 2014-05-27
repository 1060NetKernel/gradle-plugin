package org.netkernel.gradle.plugin.util

/**
 * A helper class for interacting with the file systems.
 */
class FileSystemHelper {

    // Added for test purposes
    String _gradleHome

    /**
     * Check whether the specified directory exists or not.
     *
     * @param filename directory or file to check for existence
     *
     * @return true if directory exists, false otherwise
     */
    boolean exists(String filename) {
        new File(filename)?.exists()
    }

    /**
     * Get the Gradle Home directory for this user.
     * @return the current user's Gradle home dir
     */
    def gradleHomeDir() {
        if (_gradleHome) {
            return _gradleHome
        } else {
            return "${System.properties['user.home']}/.gradle"
        }
    }

    /**
     * Creates the specified directory
     *
     * @param dirName name of directory to create
     *
     * @return true if directory is created successfully, false otherwise
     */
    boolean createDirectory(String dirName) {
        boolean retValue = false

        def f = new File(dirName)
        retValue = f.isDirectory() && f.exists()

        if (!retValue) {
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
    String dirInGradleHomeDirectory(def dirName) {
        "${gradleHomeDir()}/$dirName"
    }

    boolean dirExistsInGradleHomeDirectory(def dirName) {
        return exists(dirInGradleHomeDirectory(dirName))
    }

    /**
     * Create the specified directory in the user's Gradle Home directory.
     */
    boolean createDirInGradleHomeDirectory(def dirName) {
        def dir = "${gradleHomeDir()}/$dirName"
        return createDirectory(dir)
    }

}
