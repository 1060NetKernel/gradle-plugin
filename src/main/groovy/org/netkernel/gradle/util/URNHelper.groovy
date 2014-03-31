package org.netkernel.gradle.util

import org.gradle.api.Project

/**
 * Created by randolph.kahle on 3/30/14.
 */
class URNHelper {

    def String urnToUrnCore(String urn) {
      return urn.substring(0,urn.lastIndexOf(':'))
    }

    def String urnToDirectoryName(String urn) {
        return urn.replace(':', '.')
    }

    def String urnToResPath(String urn) {
        return 'res:/' + urn.substring(4).replaceAll(':','/')
    }

    def String urnToCorePackage(String urn) {
        return urnToUrnCore(urn).substring(4).replaceAll(':', '.')
    }

}
