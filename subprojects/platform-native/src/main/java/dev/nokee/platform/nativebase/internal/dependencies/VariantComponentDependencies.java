package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VariantComponentDependencies<T extends NativeComponentDependencies> {
	@Getter private final T dependencies;
	@Getter private final NativeIncomingDependencies incoming;
	@Getter private final NativeOutgoingDependencies outgoing;
}
