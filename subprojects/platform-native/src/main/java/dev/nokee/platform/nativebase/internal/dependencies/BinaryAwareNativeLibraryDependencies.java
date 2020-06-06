package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public abstract class BinaryAwareNativeLibraryDependencies extends AbstractBinaryAwareNativeComponentDependencies implements NativeLibraryDependencies {
	private final NativeLibraryDependencies delegate;

	@Inject
	public BinaryAwareNativeLibraryDependencies(NativeLibraryDependencies delegate, NativeIncomingDependencies incoming, NativeOutgoingDependencies outgoing) {
		super(delegate, incoming, outgoing);
		this.delegate = delegate;
	}

	@Override
	public void api(Object notation) {
		delegate.api(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		delegate.api(notation, action);
	}
}
