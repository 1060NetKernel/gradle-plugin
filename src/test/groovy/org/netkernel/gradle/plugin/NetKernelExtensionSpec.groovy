package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.model.NetKernelInstance
import org.netkernel.gradle.plugin.nk.Download

class NetKernelExtensionSpec extends BasePluginSpec {

    Project project
    NetKernelExtension netKernelExtension

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')
        project.configurations.create('provided').extendsFrom(project.configurations.compile)
        netKernelExtension = new NetKernelExtension(project)
        netKernelExtension.fileSystemHelper.@_gradleHome = file '/test/gradleHomeDirectory'
        netKernelExtension.instances = project.container(NetKernelInstance)

//        netKernelExtension.envs {
//            test {
//                directory = file '/test/gradleHomeDirectory/netkernel/installation'
//            }
//        }

        netKernelExtension.instances {
            SE {
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

    def 'uses ROC repository'() {
        when:
        netKernelExtension.useROCRepo()

        then:
        project.repositories.find { it.name == 'maven' }.url as String == 'http://maven.netkernelroc.org:8080/netkernel-maven'
    }

    def 'uses local host repository'() {
        when:
        netKernelExtension.useLocalhostRepo()

        then:
        project.repositories.find { it.name == 'maven' }.url as String == 'http://localhost:8080/netkernel-maven'
    }

    def 'configures instances'() {
        setup:
        File devDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/dev')
        File qaDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/qa')
        File prodDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/prod')

        when:
        netKernelExtension.instances {
            dev {
                location = devDirectory
            }
            qa {
                location = qaDirectory
            }
            prod {
                location = prodDirectory
            }
        }

        then:
        netKernelExtension.instances['dev'].location == devDirectory
        netKernelExtension.instances['qa'].location == qaDirectory
        netKernelExtension.instances['prod'].location == prodDirectory
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
}
