package org.netkernel.gradle.plugin.tasks

// TODO - Change back into enum
final class TaskName {

    // NetKernel Plugin Tasks
    static final String APPOSITE_CONFIGURE = 'appositeConfigure'
    static final String APPOSITE_UPDATE = 'appositeUpdate'
    static final String CREATE_APPOSITE_PACKAGE = 'createAppositePackage'
//    static final String DEPLOY = 'deploy'
//    static final String UNDEPLOY = 'undeploy'
    static final String DOWNLOAD_EE = 'downloadEE'
    static final String DOWNLOAD_SE = 'downloadSE'
    static final String INSTALL_FREEZE = 'installFreeze'
    static final String MODULE = 'module'
    static final String MODULE_RESOURCES = 'moduleResources'
    static final String THAW = 'thaw'
    static final String THAW_CONFIGURE = 'thawConfigure'
    static final String THAW_DELETE_INSTALL = 'thawDeleteInstall'
    static final String THAW_EXPAND = 'thawExpand'
    static final String UPDATE_MODULE_XML_VERSION = 'updateModuleXmlVersion'
    static final String UPLOAD_FREEZE = 'uploadFreeze'

    // Tasks from gradle
    static final String COMPILE_GROOVY = 'compileGroovy'
    static final String JAR = 'jar'

    private TaskName() {
        // Private constructor to prevent creation
    }
//    String taskName
//
//    TaskName() {
//        String[] tokens = name().toLowerCase().split('_')
//        StringBuilder stringBuilder = new StringBuilder()
//        tokens.eachWithIndex { token, index ->
//            if(index > 0) {
//                stringBuilder.append(token.capitalize())
//            } else {
//                stringBuilder.append(token)
//            }
//        }
//        taskName = stringBuilder.toString()
//    }
//
//    Object asType(Class clazz) {
//        switch(clazz) {
//            case String:
//                return taskName
//                break;
//            default:
//                return super.asType(clazz)
//        }
//    }
}