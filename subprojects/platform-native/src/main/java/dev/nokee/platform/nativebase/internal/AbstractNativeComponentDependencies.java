package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class AbstractNativeComponentDependencies {
	private final DependencyBucket implementation;
	private final DependencyBucket compileOnly;
	private final DependencyBucket linkOnly;
	private final DependencyBucket runtimeOnly;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	protected AbstractNativeComponentDependencies(NamingScheme names) {
		this.implementation = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("implementation"), ConfigurationUtils::configureAsBucket));
		this.compileOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("compileOnly"), ConfigurationUtils::configureAsBucket));
		this.linkOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("linkOnly"), ConfigurationUtils::configureAsBucket));
		this.runtimeOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("runtimeOnly"), ConfigurationUtils::configureAsBucket));
	}

	public void implementation(Object notation) {
		implementation.addDependency(notation);
	}

	public void implementation(Object notation, Action<? super ModuleDependency> action) {
		implementation.addDependency(notation, action);
	}

	public void compileOnly(Object notation) {
		compileOnly.addDependency(notation);
	}

	public void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		compileOnly.addDependency(notation, action);
	}

	public void linkOnly(Object notation) {
		linkOnly.addDependency(notation);
	}

	public void linkOnly(Object notation, Action<? super ModuleDependency> action) {
		linkOnly.addDependency(notation, action);
	}

	public void runtimeOnly(Object notation) {
		runtimeOnly.addDependency(notation);
	}

	public void runtimeOnly(Object notation, Action<? super ModuleDependency> action) {
		runtimeOnly.addDependency(notation, action);
	}

	public Configuration getImplementationDependencies() {
		return implementation.getAsConfiguration();
	}

	public Configuration getCompileOnlyDependencies() {
		return compileOnly.getAsConfiguration();
	}

	public Configuration getLinkOnlyDependencies() {
		return linkOnly.getAsConfiguration();
	}

	public Configuration getRuntimeOnlyDependencies() {
		return runtimeOnly.getAsConfiguration();
	}
}
