package org.netkernel.gradle.plugin.model

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.BasePluginSpec

class NetKernelExtensionSpec extends BasePluginSpec {

    Project project
    NetKernelExtension netKernelExtension
    PropertyHelper mockPropertyHelper

    void setup() {
        mockPropertyHelper = Mock()

        project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')
        project.configurations.create('provided').extendsFrom(project.configurations.compile)
        netKernelExtension = new NetKernelExtension(project)
        netKernelExtension.fileSystemHelper.@_gradleHome = file '/test/gradleHomeDirectory'
        netKernelExtension.instances = project.container(NetKernelInstance)
        netKernelExtension.propertyHelper = mockPropertyHelper

//        netKernelExtension.envs {
//            test {
//                directory = file '/test/gradleHomeDirectory/netkernel/installation'
//            }
//        }

        netKernelExtension.instances {
            SE {
                backendPort = 1060
                frontendPort = 8080
                url = 'http://localhost'
                location = file '/test/gradleHomeDirectory/netkernel/installation'
            }
        }
    }

    def 'creates netkernel extension'() {
        expect:
        netKernelExtension != null
    }

    def 'uses standard compile dependencies'() {
        setup:
        Set<String> dependencies = [
            'netkernel.api', 'netkernel.impl', 'layer0', 'module.standard', 'cache.se', 'ext.layer1'
        ]

        when:
        netKernelExtension.useStandardCompileDependencies()

        then:
        project.configurations.getByName('provided') != null
        dependencies.each { dependencyName ->
            assert project.configurations.getByName('provided').dependencies.find { it.name == dependencyName }
        }
    }

    def 'uses maven central'() {
        when:
        netKernelExtension.useMavenCentral()

        then:
        project.repositories.findByName(ArtifactRepositoryContainer.DEFAULT_MAVEN_CENTRAL_REPO_NAME)
    }

    def 'uses custom maven repositories'() {
        when:
        netKernelExtension."${method}"()

        then:
        1 * mockPropertyHelper.findProjectProperty(_, propertyName) >> "http://localhost"
        project.repositories.find { it.name == 'maven' }.url as String == "http://localhost"

        where:
        method             | propertyName
        'useLocalhostRepo' | PropertyHelper.MAVEN_LOCAL_URL
        'useNKRepo'        | PropertyHelper.MAVEN_NETKERNEL_URL
        //'useROCRepo'       | PropertyHelper.MAVEN_NETKERNELROC_URL

    }

    def 'configures instances'() {
        setup:
        File devDirectory = file '/test/netKernelExtensionSpec/opt/netkernel/dev'
        File prodDirectory = file '/test/netKernelExtensionSpec/opt/netkernel/prod'

        Map projectProperties = [
            (PropertyHelper.NETKERNEL_INSTANCE_BACKEND_PORT)     : 1060,
            (PropertyHelper.NETKERNEL_INSTANCE_FRONTEND_PORT)    : 8080,
            (PropertyHelper.NETKERNEL_INSTANCE_DEFAULT_URL)      : 'http://localhost',
            (PropertyHelper.CURRENT_MAJOR_RELEASE_VERSION)       : '5.1.2',
            (PropertyHelper.NETKERNEL_INSTANCE_DOWNLOAD_JAR_NAME): 'nk.jar'
        ]

        when:
        netKernelExtension.instances {
            DEV {
                location = devDirectory.absolutePath
            }
            PROD {
                edition = 'EE'
                version = '5.1.1'
                location = prodDirectory.absolutePath
                url = 'http://127.0.0.1'
                backendPort = 2200
                frontendPort = 8888
            }
        }

        then:
        _ * mockPropertyHelper.findProjectProperty(*_) >> { List args ->
            return projectProperties[args[1]]
        }

        netKernelExtension.instances['DEV'].location == devDirectory
        netKernelExtension.instances['DEV'].jarFileLocation.name == 'nk.jar'
        netKernelExtension.instances['DEV'].edition == Edition.STANDARD
        netKernelExtension.instances['DEV'].netKernelVersion == '5.1.2'
        netKernelExtension.instances['DEV'].url as String == 'http://localhost'
        netKernelExtension.instances['DEV'].backendPort == 1060
        netKernelExtension.instances['DEV'].frontendPort == 8080

        netKernelExtension.instances['PROD'].location == prodDirectory
        netKernelExtension.instances['PROD'].jarFileLocation.name == 'nk.jar'
        netKernelExtension.instances['PROD'].edition == Edition.ENTERPRISE
        netKernelExtension.instances['PROD'].netKernelVersion == '5.1.1'
        netKernelExtension.instances['PROD'].url as String == 'http://127.0.0.1'
        netKernelExtension.instances['PROD'].backendPort == 2200
        netKernelExtension.instances['PROD'].frontendPort == 8888
    }

    def 'configures download object'() {
        when:
        Download download = netKernelExtension.download {
            se {
                url = "se_url"
                username = "se_username"
                password = "se_password"
            }
            ee {
                url = "ee_url"
                username = "ee_username"
                password = "ee_password"
            }
        }

        then:
        download.se.url == "se_url"
        download.se.username == "se_username"
        download.se.password == "se_password"

        download.ee.url == "ee_url"
        download.ee.username == "ee_username"
        download.ee.password == "ee_password"
    }

    def 'gets directories'() {
        setup:
        File expectedDirectory = file(path)

        when:
        File directory = netKernelExtension."${directoryName}"

        then:
        _ * mockPropertyHelper.findProjectProperty(project, PropertyHelper.NETKERNEL_INSTANCE, 'SE') >> 'SE'
        directory == expectedDirectory

        where:
        directoryName               | path
        'destinationDirectory'      | '/test/gradleHomeDirectory/netkernel'
        'freezeDirectory'           | '/test/gradleHomeDirectory/netkernel/freeze'
        'installationDirectory'     | '/test/gradleHomeDirectory/netkernel/installation'
        'thawDirectory'             | '/test/gradleHomeDirectory/netkernel/thaw'
        'thawInstallationDirectory' | '/test/gradleHomeDirectory/netkernel/thawInstallation'
    }

    def 'gets frozen archive file'() {
        setup:
        File expectedFrozenArchiveFile = file '/test/gradleHomeDirectory/netkernel/download/frozen.zip'

        when:
        File frozenArchiveFile = netKernelExtension.frozenArchiveFile

        then:
        frozenArchiveFile == expectedFrozenArchiveFile
    }

    def 'gets work file'() {
        setup:
        File expectedFile = file '/test/gradleHomeDirectory/netkernel/download'

        when:
        File workFile = netKernelExtension.workFile('download')

        then:
        workFile == expectedFile
    }

    def 'gets distribution jar file'() {
        setup:
        Edition edition = Edition.STANDARD
        String version = '5.2.1'

        when:
        String jarFile = netKernelExtension.distributionJarFile(edition, version)

        then:
        1 * mockPropertyHelper.findProjectProperty(_, _, _, _) >> { Project project, String propertyName, String defaultValue, Map values ->
            assert values['edition'] == edition
            assert values['netKernelVersion'] == version
            return "jarFile"
        }
        jarFile == "jarFile"
    }

    def 'gets project property'() {
        when:
        String propertyValue = netKernelExtension.projectProperty('propertyName', 'defaultValue', [a: 'a'])

        then:
        1 * mockPropertyHelper.findProjectProperty(_, _, _, _) >> { Project project, String propertyName, String defaultValue, Map values ->
            assert values['a'] == 'a'
            assert propertyName == 'propertyName'
            assert defaultValue == 'defaultValue'
            return "propertyValue"
        }
        propertyValue == 'propertyValue'
    }
}
