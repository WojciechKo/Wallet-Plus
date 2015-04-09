package pl.net.korzeniowski.walletplus;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class MyRobolectricTestRunner extends RobolectricTestRunner {
    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws org.junit.runners.model.InitializationError if junit says so
     */
    public MyRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected SdkConfig pickSdkVersion(AndroidManifest appManifest, Config config) {
        return new SdkConfig(21);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String basePath = "./src/main/";
        return new AndroidManifest(
                Fs.fileFromPath(basePath + "AndroidManifest.xml"),
                Fs.fileFromPath(basePath + "res"),
                Fs.fileFromPath(basePath + "assets"));
    }
}
