package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class JniLibraryNativeDependenciesInternal implements JniLibraryNativeDependencies {
	private final DefaultNativeComponentDependencies delegate;
	@Getter private final NativeIncomingDependencies incoming;

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	public JniLibraryNativeDependenciesInternal(DefaultNativeComponentDependencies delegate, NativeIncomingDependencies incoming) {
		this.delegate = delegate;
		this.incoming = incoming;
	}

	@Override
	public void nativeImplementation(Object notation) {
		delegate.implementation(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		delegate.implementation(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		delegate.linkOnly(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.linkOnly(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		delegate.runtimeOnly(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.runtimeOnly(notation, action);
	}

	public Configuration getNativeImplementationDependencies() {
		return delegate.getImplementationDependencies();
	}

	public Configuration getNativeLinkOnlyDependencies() {
		return delegate.getLinkOnlyDependencies();
	}

	public Configuration getNativeRuntimeOnlyDependencies() {
		return delegate.getRuntimeOnlyDependencies();
	}
}
