package dev.nokee.runtime.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultNativeComponentDependencies implements NativeComponentDependencies {
	private final DependencyBucket implementation;
	private final DependencyBucket compileOnly;
	private final DependencyBucket linkOnly;
	private final DependencyBucket runtimeOnly;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	public DefaultNativeComponentDependencies(NamingScheme names) {
		this.implementation = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create("implementation", ConfigurationUtils::configureAsBucket));
		this.compileOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create("compileOnly", ConfigurationUtils::configureAsBucket));
		this.linkOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create("linkOnly", ConfigurationUtils::configureAsBucket));
		this.runtimeOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create("runtimeOnly", ConfigurationUtils::configureAsBucket));
	}

	@Override
	public void implementation(Object notation) {
		implementation.addDependency(notation);
	}

	@Override
	public void implementation(Object notation, Action<? super ModuleDependency> action) {
		implementation.addDependency(notation, action);
	}

	@Override
	public void compileOnly(Object notation) {
		compileOnly.addDependency(notation);
	}

	@Override
	public void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		compileOnly.addDependency(notation, action);
	}

	@Override
	public void linkOnly(Object notation) {
		linkOnly.addDependency(notation);
	}

	@Override
	public void linkOnly(Object notation, Action<? super ModuleDependency> action) {
		linkOnly.addDependency(notation, action);
	}

	@Override
	public void runtimeOnly(Object notation) {
		runtimeOnly.addDependency(notation);
	}

	@Override
	public void runtimeOnly(Object notation, Action<? super ModuleDependency> action) {
		runtimeOnly.addDependency(notation, action);
	}
}
