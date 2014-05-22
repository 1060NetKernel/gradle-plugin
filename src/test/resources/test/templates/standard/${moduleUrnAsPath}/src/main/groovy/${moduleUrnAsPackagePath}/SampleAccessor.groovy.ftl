package ${moduleUrnAsPackage}

import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

class SampleAccessor extends StandardAccessorImpl {

    @Override
    void onSource(INKFRequestContext context) throws Exception {
        context.createResponseFrom("hello there!")
    }

}