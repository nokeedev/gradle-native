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

//	@Override
//	public DependencyBucket create(String name) {
//		return super.create("native" + StringUtils.capitalize(name));
//	}
//
//	@Override
//	public DependencyBucket create(String name, Action<Configuration> action) {
//		return super.create("native" + StringUtils.capitalize(name), action);
//	}

	private void configureImplementationConfiguration(Configuration configuration) {
	}

	private void configureLinkOnlyConfiguration(Configuration configuration) {
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
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
