gradle-plugin
=============

Gradle Plugin to provide build, deploy, module templating functionality and more



## Templates

The Gradle plugin supports the creation of NetKernel modules from a template.

### Commands

Command | Abbreviation | Description
--- | --- | ---
listTemplates | lT | Examines all template libraries specified as a dependency and their templates.
createNetKernelModuleFromTemplate | cNKMFT | Creates a new NetKernel module based on a specified template and with additional input from the user gather during a dialog

### Required Information

Information is required to configure each created module.
The module template author can use substitution variables in their
template which will be replaced by computing information derived
from information provided by the plugin user.

When the user runs the ''createNetKernelModuleFromTemplate'' command they will be asked provide the
following information:

Information | Description
--- | ---
Template Name | The user must type in the name of the template to use from the library. The command will list all available templates before asking for this information.
Module URN | This is the unique identifier for the module. For example, ''org:netkernelroc:lang:scala'' .
Module Name | This is a descriptive name such as Scala Language
Description | A brief description about the module. For example: "Scala language support"
ROC Space name | This is the name displayed by the Space Explorer. For example: "Lang / Scala"
Version Number | This is the initial module version number. For example: "0.1.0"


### Configuration

Example build.gradle file for creating modules:

    apply plugin: 'netkerneltemplates'
    apply plugin: 'java'
    apply plugin: 'maven'

    repositories {
      mavenLocal()
      mavenCentral( )
      maven {
        url "http://maven.netkernelroc.org:8080/netkernel-maven"
      }
    }

    buildscript {
      repositories {
        mavenLocal()
        mavenCentral()
        maven {
          url "http://maven.netkernelroc.org:8080/netkernel-maven"
        }
      }
      dependencies {
        classpath group: 'org.netkernel', name: 'gradle-plugin', version: '0.0.2'
      }
    }

    dependencies {
      templates group: 'org.netkernelroc', name: 'module-template-library', version: '[0.2.0,)'
    }





To create a new module information such as the module's URN is required.

