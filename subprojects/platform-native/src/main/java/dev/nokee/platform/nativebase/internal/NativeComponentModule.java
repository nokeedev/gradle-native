package dev.nokee.platform.nativebase.internal;

import dagger.Module;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;

@Module(includes = {PlatformNativeDependenciesModule.class})
public interface NativeComponentModule {
}
