package org.netkernel.single

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl
import org.netkernel.ext.system.representation.IRepDeployedModules;

class SingleAccessor extends StandardAccessorImpl {
    @Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{
        IRepDeployedModules rep;
        aContext.createResponseFrom("Hello Brave New World");
	}
}