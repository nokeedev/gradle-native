package dev.nokee.fixtures;

import org.gradle.internal.nativeintegration.services.NativeServices;

import java.io.File;

public class NativeServicesTestFixture {
    static NativeServices nativeServices;
    static boolean initialized;

    public static synchronized void initialize() {
        if (!initialized) {
            System.setProperty("org.gradle.native", "true");
            File nativeDir = getNativeServicesDir();
            NativeServices.initialize(nativeDir);
            initialized = true;
        }
    }

    public static synchronized NativeServices getInstance() {
        if (nativeServices == null) {
            initialize();
            nativeServices = NativeServices.getInstance();
        }
        return nativeServices;
    }

    public static File getNativeServicesDir() {
        return new File("build/native-libs");
    }
}
