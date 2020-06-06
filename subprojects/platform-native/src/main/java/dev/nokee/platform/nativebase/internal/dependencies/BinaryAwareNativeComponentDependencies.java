package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.nativebase.NativeComponentDependencies;

import javax.inject.Inject;

public abstract class BinaryAwareNativeComponentDependencies extends AbstractBinaryAwareNativeComponentDependencies implements NativeComponentDependencies {
	@Inject
	public BinaryAwareNativeComponentDependencies(NativeComponentDependencies delegate, NativeIncomingDependencies incoming, NativeOutgoingDependencies outgoing) {
		super(delegate, incoming, outgoing);
	}
}
