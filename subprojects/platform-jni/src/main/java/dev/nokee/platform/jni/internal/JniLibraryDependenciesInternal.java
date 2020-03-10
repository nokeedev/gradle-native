package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniLibraryDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import javax.inject.Inject;

public abstract class JniLibraryDependenciesInternal implements JniLibraryDependencies {

	private final Configuration apiDependencies;
	private final Configuration jvmImplementationDependencies;
	private final Configuration nativeImplementationDependencies;

	@Inject
	public JniLibraryDependenciesInternal(ConfigurationContainer configurations) {
		apiDependencies = configurations.create("api", JniLibraryDependenciesInternal::bucket);
		jvmImplementationDependencies = configurations.create("jvmImplementation", JniLibraryDependenciesInternal::bucket);
		nativeImplementationDependencies = configurations.create("nativeImplementation", JniLibraryDependenciesInternal::bucket);

		jvmImplementationDependencies.extendsFrom(apiDependencies);
	}

	private static void bucket(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(false);
	}

	@Inject
	protected abstract DependencyHandler getDependencyHandler();

	@Override
	public void api(Object notation) {
		apiDependencies.getDependencies().add(getDependencyHandler().create(notation));
	}

	@Override
	public void api(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		apiDependencies.getDependencies().add(dependency);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementationDependencies.getDependencies().add(getDependencyHandler().create(notation));
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		jvmImplementationDependencies.getDependencies().add(dependency);
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeImplementationDependencies.getDependencies().add(getDependencyHandler().create(notation));
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		nativeImplementationDependencies.getDependencies().add(dependency);
	}

	public Configuration getApiDependencies() {
		return apiDependencies;
	}

	public Configuration getNativeDependencies() {
		return nativeImplementationDependencies;
	}

	public Configuration getJvmDependencies() {
		return jvmImplementationDependencies;
	}
}
