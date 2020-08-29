package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DeclarableMacOsFrameworkAwareDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

public final class DefaultJavaNativeInterfaceNativeComponentDependencies extends BaseComponentDependenciesContainer implements JavaNativeInterfaceNativeComponentDependencies, ComponentDependencies {
	@Getter private final DeclarableDependencyBucket nativeImplementation;
	@Getter private final DeclarableDependencyBucket nativeLinkOnly;
	@Getter private final DeclarableDependencyBucket nativeRuntimeOnly;

	public DefaultJavaNativeInterfaceNativeComponentDependencies(ComponentDependenciesContainer delegate) {
		super(delegate);
		this.nativeImplementation = delegate.register(DependencyBucketName.of("nativeImplementation"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureImplementationBucket);
		this.nativeLinkOnly = delegate.register(DependencyBucketName.of("nativeLinkOnly"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureLinkOnlyBucket);
		this.nativeRuntimeOnly = delegate.register(DependencyBucketName.of("nativeRuntimeOnly"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureRuntimeOnlyBucket);
	}

	private void configureImplementationBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {}

	private void configureLinkOnlyBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {
		bucket.extendsFrom(nativeImplementation);
	}

	private void configureRuntimeOnlyBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {
		bucket.extendsFrom(nativeImplementation);
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeImplementation.addDependency(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		nativeImplementation.addDependency(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		nativeLinkOnly.addDependency(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeLinkOnly.addDependency(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		nativeRuntimeOnly.addDependency(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeRuntimeOnly.addDependency(notation, action);
	}
}
