package dev.nokee.language.nativebase.internal.toolchains;

import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;

public interface KnownAwareToolChain {
	boolean isKnown(NativePlatformInternal nativePlatform);
}
