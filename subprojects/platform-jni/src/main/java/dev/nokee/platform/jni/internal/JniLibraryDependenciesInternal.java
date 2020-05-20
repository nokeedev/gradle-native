package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExternalModuleDependency;

import javax.inject.Inject;

// TODO: Add tests for jvmRuntimeOnly
public abstract class JniLibraryDependenciesInternal extends JniLibraryNativeDependenciesInternal implements JniLibraryDependencies {
	private final Configuration apiDependencies;
	private final Configuration jvmImplementationDependencies;
	private final Configuration jvmRuntimeOnly;

	@Inject
	public JniLibraryDependenciesInternal() {
		Configuration api = getConfigurations().findByName("api");
		if (api == null) {
			apiDependencies = getConfigurations().create("api", ConfigurationUtils::configureAsBucket);
		} else {
			apiDependencies = api;
		}
		jvmImplementationDependencies = getConfigurations().create("jvmImplementation", ConfigurationUtils::configureAsBucket);
		jvmRuntimeOnly = getConfigurations().create("jvmRuntimeOnly", ConfigurationUtils::configureAsBucket);
		jvmImplementationDependencies.extendsFrom(apiDependencies);
	}

	@Override
	public void api(Object notation) {
		apiDependencies.getDependencies().add(getDependencies().create(notation));
	}

	@Override
	public void api(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		apiDependencies.getDependencies().add(dependency);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementationDependencies.getDependencies().add(getDependencies().create(notation));
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		jvmImplementationDependencies.getDependencies().add(dependency);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.getDependencies().add(getDependencies().create(notation));
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		jvmRuntimeOnly.getDependencies().add(dependency);
	}

	public Configuration getApiDependencies() {
		return apiDependencies;
	}

	public Configuration getJvmDependencies() {
		return jvmImplementationDependencies;
	}
}
