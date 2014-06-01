package org.netkernel.gradle.plugin.util

/**
 * A helper class for interacting with the file systems.
 */
class FileSystemHelper {

    // Added for test purposes
    File _gradleHome

    /**
     * Check whether the specified directory exists or not.
     *
     * @param filename directory or file to check for existence
     *
     * @return true if directory exists, false otherwise
     */
    boolean exists(String filename) {
        return exists(new File(filename))
    }

    /**
     * Checks whether the file exists or not. Used internally by this class.
     *
     * @param filename directory or file to check for existence
     *
     * @return true if directory exists, false otherwise
     */
    boolean exists(File file) {
        return file?.exists()
    }

    /**
     * Get the Gradle Home directory for this user.
     * @return the current user's Gradle home dir
     */
    File gradleHomeDir() {
        if (_gradleHome) {
            return _gradleHome
        } else {
            return new File("${System.properties['user.home']}/.gradle")
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
     */
//    @Deprecated
//    File dirInGradleHomeDirectory(String directoryName) {
//        new File(gradleHomeDir(), directoryName)
//    }


    /**
     * Retrieve the name of the specified file in relation to the user's gradle
     * home directory.
     */
    File fileInGradleHome(String fileName) {
        new File(gradleHomeDir(), fileName)
    }

    /**
     * Checks whether the file exists in the gradle home directory or not.
     *
     * @param directoryName
     *
     * @return true if the directory exists; false otherwise
     */
    boolean dirExistsInGradleHomeDirectory(String directoryName) {
        return exists(fileInGradleHome(directoryName))
    }

    /**
     * Create the specified directory in the user's Gradle Home directory.
     */
    boolean createDirInGradleHomeDirectory(String directoryName) {
        def dir = "${gradleHomeDir()}/$directoryName"
        return createDirectory(dir)
    }

}
