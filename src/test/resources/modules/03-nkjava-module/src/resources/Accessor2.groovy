package resources;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.apache.commons.compress.utils.ArchiveUtils;

class SingleAccessor extends StandardAccessorImpl
{
	@Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{	ArchiveUtils.toAsciiBytes("Hello Brave New World");
        aContext.createResponseFrom("Hello Brave New World");
	}
}