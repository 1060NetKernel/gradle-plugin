package ${moduleUrnAsPackage}

import org.netkernelroc.test.BaseIntegrationSpec
import org.netkernelroc.test.NetKernelResponse

class SampleAccessorSpec extends BaseIntegrationSpec {

    void setup() {
        DEFAULT_SPACE = '${moduleUrn}'
    }

    def "calls sample access"() {
        when:
        NetKernelResponse response = source {
            identifier = "active:sample"
        }

        then:
        response != null
    }

}