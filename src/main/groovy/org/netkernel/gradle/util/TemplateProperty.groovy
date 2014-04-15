package org.netkernel.gradle.util

/**
 * Template Property represents the actual values that can be contained within templates.  For example, when writing
 * a module.xml file, you can use the MODULE_URN like so:
 *
 * <uri>${MODULE_URN}</uri>
 *
 * More details can be found in src/test/resources/test.
 */
class TemplateProperty {

    static final String MODULE_SPACE_NAME = "moduleSpaceName"
    static final String MODULE_DESCRIPTION = "moduleDescription"
    static final String MODULE_VERSION = "moduleVersion"
    static final String MODULE_NAME = "moduleName"
    static final String MODULE_URN_CORE_PACKAGE = "moduleUrnCorePackage"
    static final String MODULE_URN_RES_PATH_CORE = "moduleUrnResPathCore"
    static final String MODULE_URN_RES_PATH = "moduleUrnResPath"
    static final String MODULE_URN_CORE = "moduleUrnCore"
    static final String MODULE_URN = "moduleUrn"
    static final String MODULE_DIRECTORY = "moduleDirectory"

}