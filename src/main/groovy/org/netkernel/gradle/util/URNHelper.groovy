package org.netkernel.gradle.util
/**
 * Created by randolph.kahle on 3/30/14.
 */
class URNHelper {

    String urnToUrnCore(String urn) {
        return urn.substring(0, urn.lastIndexOf(':'))
    }

    String urnToDirectoryName(String urn) {
        return urn.replace(':', '.')
    }

    String urnToResPath(String urn) {
        return 'res:/' + urn.substring(4).replaceAll(':', '/')
    }

    String urnToCorePackage(String urn) {
        return urnToUrnCore(urn).substring(4).replaceAll(':', '.')
    }

}
