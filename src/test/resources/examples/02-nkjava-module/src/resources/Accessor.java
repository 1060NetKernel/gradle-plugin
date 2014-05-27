package resources;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

class SingleAccessor extends StandardAccessorImpl
{
	@Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{	aContext.createResponseFrom("Hello Brave New World");
	}
}