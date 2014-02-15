package org.netkernel.single

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl

class SingleAccessor extends StandardAccessorImpl {
    def static main(args) {
        println "Huzzah!!!"
    }
    
    @Override
	public void onSource(INKFRequestContext aContext) throws Exception
	{	aContext.createResponseFrom("Hello Brave New World");
	}
}