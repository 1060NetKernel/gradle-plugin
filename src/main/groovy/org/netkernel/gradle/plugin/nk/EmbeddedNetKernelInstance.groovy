package org.netkernel.gradle.plugin.nk

/*
import com.ten60.netkernel.cache.se.representation2.ConcurrentCache
import org.netkernel.container.impl.Kernel
import org.netkernel.layer0.boot.IModuleFactory
import org.netkernel.layer0.boot.ModuleManager
import org.netkernel.layer0.logging.LogManager
import org.netkernel.layer0.nkf.INKFRequestContext
import org.netkernel.layer0.tools.ExtraMimeTypes
import org.netkernel.layer0.util.PropertyConfiguration
import org.netkernel.module.standard.StandardModuleFactory
*/


/**
 * A class to allow NetKernel to start as
 * part of the build and test process.
 *
 */
class EmbeddedNetKernelInstance {
    /*
    def ModuleManager mModuleManager
    def INKFRequestContext mContext
    def ConcurrentCache mRepresentationCache
    */

    EmbeddedNetKernelInstance() {
        try {
            init()
        } catch(Throwable t) {
            t.printStackTrace()
        }
    }

    def init() throws Exception {
        /*
        Kernel kernel = new Kernel()
        ClassLoader classLoader = EmbeddedNetKernelInstance.class.getClassLoader()
        ExtraMimeTypes.getInstance()
        def logger = new LogManager(null).getKernelLogger()


        URL kernelProperties = classLoader.getResource("kernel.properties")
        PropertyConfiguration config = new PropertyConfiguration(kernelProperties, logger)
        config.setProperty("netkernel.boot.time", Long.toString(System.currentTimeMillis()))
        kernel.setConfiguration(config)
        kernel.setLogger(logger)

        IModuleFactory[] factories = [ new StandardModuleFactory() ]

        mModuleManager = new ModuleManager(kernel, factories)
        //InputStream is = classLoader.getResourceAsStream

        mModuleManager.setRunLevel(1)
        */
    }
}
