package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

final class NativeComponentDependenciesImpl extends BaseComponentDependenciesContainer implements NativeComponentDependencies, NativeComponentDependenciesInternal, ComponentDependencies {
	@Getter private final DeclarableDependencyBucket implementation;
	@Getter private final DeclarableDependencyBucket compileOnly;
	@Getter private final DeclarableDependencyBucket linkOnly;
	@Getter private final DeclarableDependencyBucket runtimeOnly;

	public NativeComponentDependenciesImpl(ComponentDependenciesContainer delegate) {
		super(delegate);
		this.implementation = delegate.register(DependencyBucketName.of("implementation"), DeclarableMacOsFrameworkAwareDependencies.class,  this::configureImplementationBucket);
		this.compileOnly = delegate.register(DependencyBucketName.of("compileOnly"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureCompileOnlyBucket);
		this.linkOnly = delegate.register(DependencyBucketName.of("linkOnly"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureLinkOnlyBucket);
		this.runtimeOnly = delegate.register(DependencyBucketName.of("runtimeOnly"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureRuntimeOnlyConfiguration);
	}

	private void configureImplementationBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {}

	private void configureCompileOnlyBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {
		bucket.extendsFrom(implementation);
	}

	private void configureLinkOnlyBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {
		bucket.extendsFrom(implementation);
	}

	private void configureRuntimeOnlyConfiguration(DeclarableMacOsFrameworkAwareDependencies bucket) {
		bucket.extendsFrom(implementation);
	}

	@Override
	public void implementation(Object notation) {
		getImplementation().addDependency(notation);
	}

	@Override
	public void implementation(Object notation, Action<? super ModuleDependency> action) {
		getImplementation().addDependency(notation, action);
	}

	@Override
	public void compileOnly(Object notation) {
		getCompileOnly().addDependency(notation);
	}

	@Override
	public void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		getCompileOnly().addDependency(notation, action);
	}

	@Override
	public void linkOnly(Object notation) {
		getLinkOnly().addDependency(notation);
	}

	@Override
	public void linkOnly(Object notation, Action<? super ModuleDependency> action) {
		getLinkOnly().addDependency(notation, action);
	}

	@Override
	public void runtimeOnly(Object notation) {
		getRuntimeOnly().addDependency(notation);
	}

	@Override
	public void runtimeOnly(Object notation, Action<? super ModuleDependency> action) {
		getRuntimeOnly().addDependency(notation, action);
	}
}
