package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx.configureAsBucket;

public class DefaultJavaNativeInterfaceNativeComponentDependencies extends BaseComponentDependencies implements JavaNativeInterfaceNativeComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket nativeImplementation;
	@Getter private final DependencyBucket nativeLinkOnly;
	@Getter private final DependencyBucket nativeRuntimeOnly;

	@Inject
	public DefaultJavaNativeInterfaceNativeComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.nativeImplementation = delegate.create("nativeImplementation", this::configureImplementationConfiguration);
		this.nativeLinkOnly = delegate.create("nativeLinkOnly", this::configureLinkOnlyConfiguration);
		this.nativeRuntimeOnly = delegate.create("nativeRuntimeOnly", this::configureRuntimeOnlyConfiguration);
	}

	private void configureImplementationConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.setDescription(String.format("Implementation only dependencies for %s.", getComponentDisplayName()));
	}

	private void configureLinkOnlyConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
		configuration.setDescription(String.format("Link only dependencies for %s.", getComponentDisplayName()));
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
		configuration.setDescription(String.format("Runtime only dependencies for %s.", getComponentDisplayName()));
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
