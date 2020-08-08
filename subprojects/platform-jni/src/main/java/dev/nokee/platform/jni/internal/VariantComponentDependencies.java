package dev.nokee.platform.jni.internal;

import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VariantComponentDependencies {
	@Getter private final DefaultJavaNativeInterfaceNativeComponentDependencies dependencies;
	@Getter private final NativeIncomingDependencies incoming;
}
