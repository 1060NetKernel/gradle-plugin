gradle-plugin
=============

Gradle Plugin to provide build, deploy, module templating functionality and more



## Templates

The Gradle plugin supports the creation of NetKernel modules from a template.

### Commands

Command | Abbreviation | Description
--- | --- | ---
listTemplates | lT | Examines all template libraries specified as a dependency and their templates.
createModule | cM | Creates a new NetKernel module based on a specified template and with additional input from the user gather during a dialog

### Required Information

Information is required to configure each created module.
The module template author can use substitution variables in their
template which will be replaced by computing information derived
from information provided by the plugin user.

When the user runs the ''createModule'' command they will be asked provide the
following information:

Information | Description
--- | ---
Template Name | The user must type in the name of the template to use from the library. The command will list all available templates before asking for this information.
Module URN | This is the unique identifier for the module. For example, ''org:netkernelroc:lang:scala'' .


### Configuration

Templates can be loaded by using gradle's dependency mechanism or from local folders.

Example build.gradle file for creating modules:

```groovy
    apply plugin: 'netkerneltemplates'
    apply plugin: 'java'
    apply plugin: 'maven'

    repositories {
      mavenLocal()
      mavenCentral( )
      maven {
        url "http://maven.netkernel.org/netkernel-maven"
      }
    }

    buildscript {
      repositories {
        mavenLocal()
        mavenCentral()
        maven {
          url "http://maven.netkernel.org/netkernel-maven"
        }
      }
      dependencies {
        classpath group: 'urn.org.netkernel', name: 'gradle-plugin', version: '1.1.1'
      }
    }

    dependencies {
      templates group: 'org.netkernelroc', name: 'module-template-library', version: '[0.2.0,)'
    }
```

Additional templates can be loaded by adding a comma separated list of directories to your ~/.gradle/gradle.properties file:

```
  netkernel.template.dirs=~/templates1, ~/templates2
```

// Make reference to gradle-download-task
https://github.com/michel-kraemer/gradle-download-task