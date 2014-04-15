package org.netkernel.gradle.util

/**
 * Template Property represents the actual values that can be contained within templates.  For example, when writing
 * a module.xml file, you can use the MODULE_URN like so:
 *
 * <uri>${MODULE_URN}</uri>
 *
 * More details can be found in src/test/resources/test.
 */
enum TemplateProperty {

    MODULE_SPACE_NAME("moduleSpaceName"),
    MODULE_DESCRIPTION("moduleDescription"),
    MODULE_VERSION("moduleVersion"),
    MODULE_NAME("moduleName"),
    MODULE_URN_CORE_PACKAGE("moduleUrnCorePackage"),
    MODULE_URN_RES_PATH_CORE("moduleUrnResPathCore"),
    MODULE_URN_RES_PATH("moduleUrnResPath"),
    MODULE_URN_CORE("moduleUrnCore"),
    MODULE_URN("moduleUrn"),
    MODULE_DIRECTORY("moduleDirectory"),
    NETKERNEL_TEMPLATE_DIRS('netkernel.template.dirs')

    String value

    TemplateProperty(String value) {
        this.value = value
    }

    Object asType(Class clazz) {
        if (clazz == String) {
            return value
        } else {
            return super.asType(clazz)
        }
    }

    String toString() {
        return value
    }

}