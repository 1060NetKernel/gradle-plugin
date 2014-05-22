package org.netkernel.gradle.basic

import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

class SimpleAccessor extends StandardAccessorImpl {

    @Override
    public void onSource(INKFRequestContext context) throws Exception {
        context.createResponseFrom("Hello from simple accessor");
    }
}