package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

public class AbstractBinaryAwareNativeComponentDependencies {
	private final NativeComponentDependencies delegate;
	private final NativeIncomingDependencies incoming;
	private final NativeOutgoingDependencies outgoing;

	public AbstractBinaryAwareNativeComponentDependencies(NativeComponentDependencies delegate, NativeIncomingDependencies incoming, NativeOutgoingDependencies outgoing) {
		this.delegate = delegate;
		this.incoming = incoming;
		this.outgoing = outgoing;
	}

	public void implementation(Object notation) {
		delegate.implementation(notation);
	}

	public void implementation(Object notation, Action<? super ModuleDependency> action) {
		delegate.implementation(notation, action);
	}

	public void compileOnly(Object notation) {
		delegate.compileOnly(notation);
	}

	public void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.compileOnly(notation, action);
	}

	public void linkOnly(Object notation) {
		delegate.linkOnly(notation);
	}

	public void linkOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.linkOnly(notation, action);
	}

	public void runtimeOnly(Object notation) {
		delegate.runtimeOnly(notation);
	}

	public void runtimeOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.runtimeOnly(notation, action);
	}

	public NativeIncomingDependencies getIncoming() {
		return incoming;
	}

	public NativeOutgoingDependencies getOutgoing() {
		return outgoing;
	}
}
