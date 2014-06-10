package org.netkernel.single

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl
import org.netkernel.ext.system.representation.IRepDeployedModules;

//External dependency found from NK Maven http.client expanded libs
import org.apache.http.entity.mime.MIME;

class SingleAccessor extends StandardAccessorImpl {
    @Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{
        IRepDeployedModules rep;
        aContext.createResponseFrom("Hello Brave New World");
	}
}