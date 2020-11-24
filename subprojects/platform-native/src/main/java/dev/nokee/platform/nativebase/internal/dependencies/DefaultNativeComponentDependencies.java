package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public class DefaultNativeComponentDependencies extends BaseComponentDependencies implements NativeComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket implementation;
	@Getter private final DependencyBucket compileOnly;
	@Getter private final DependencyBucket linkOnly;
	@Getter private final DependencyBucket runtimeOnly;

	@Inject
	public DefaultNativeComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.implementation = delegate.create("implementation", this::configureImplementationConfiguration);
		this.compileOnly = delegate.create("compileOnly", this::configureCompileOnlyConfiguration);
		this.linkOnly = delegate.create("linkOnly", this::configureLinkOnlyConfiguration);
		this.runtimeOnly = delegate.create("runtimeOnly", this::configureRuntimeOnlyConfiguration);
	}

	private void configureImplementationConfiguration(Configuration configuration) {
	}

	private void configureCompileOnlyConfiguration(Configuration configuration) {
	}

	private void configureLinkOnlyConfiguration(Configuration configuration) {
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
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
