package org.netkernel.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.testfixtures.ProjectBuilder
import org.netkernel.gradle.plugin.nk.Download
import org.netkernel.gradle.plugin.nk.ExecutionConfig

class NetKernelExtensionSpec extends BasePluginSpec {

    Project project
    NetKernelExtension netKernelExtension

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')
        project.configurations.create('provided').extendsFrom(project.configurations.compile)
        netKernelExtension = new NetKernelExtension(project, project.container(ExecutionConfig), 'test')
        netKernelExtension.fileSystemHelper.@_gradleHome = file '/test/gradleHomeDirectory'


        netKernelExtension.envs {
            test {
                directory = file('/test/gradleHomeDirectory/netkernel/freeze')
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

    def 'configures environment object'() {
        setup:
        File devDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/dev')
        File qaDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/qa')
        File prodDirectory = file('/test/netKernelExtensionSpec/opt/netkernel/prod')

        when:
        netKernelExtension.envs {
            dev {
                directory = devDirectory
            }
            qa {
                directory = qaDirectory
            }
            prod {
                directory = prodDirectory
            }
        }

        then:
        netKernelExtension.envs.dev.directory == devDirectory
        netKernelExtension.envs.qa.directory == qaDirectory
        netKernelExtension.envs.prod.directory == prodDirectory
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

    def 'gets installation directory'() {
        setup:
        File expectedInstallationDirectory = file '/test/gradleHomeDirectory/netkernel/freeze'

        when:
        File installationDirectory = netKernelExtension.installationDirectory

        then:
        installationDirectory == expectedInstallationDirectory
    }

    def 'gets freeze directory'() {
        setup:
        File expectedFreezeDirectory = file '/test/gradleHomeDirectory/netkernel/freeze'

        when:
        File freezeDirectory = netKernelExtension.freezeDirectory

        then:
        freezeDirectory == expectedFreezeDirectory
    }
}
