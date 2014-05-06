package org.netkernel.single

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl
import org.apache.commons.compress.zip.UnixStat;

class SingleAccessor extends StandardAccessorImpl {
    @Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{
        org.apache.commons.compress.zip.UnixStat stat;
        aContext.createResponseFrom("Hello Brave New World");
	}
}