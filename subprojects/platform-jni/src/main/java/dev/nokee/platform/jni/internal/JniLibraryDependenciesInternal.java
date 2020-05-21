package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExternalModuleDependency;

import javax.inject.Inject;
import java.util.Optional;

// TODO: Add tests for jvmRuntimeOnly
public abstract class JniLibraryDependenciesInternal extends JniLibraryNativeDependenciesInternal implements JniLibraryDependencies {
	private final Configuration apiDependencies;
	private final Configuration jvmImplementationDependencies;
	private final Configuration jvmRuntimeOnly;

	@Inject
	public JniLibraryDependenciesInternal(NamingScheme names) {
		super(names);
		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

		// Kotlin may create this configuration before us
		apiDependencies = Optional.ofNullable(getConfigurations().findByName(names.getConfigurationName("api")))
			.orElseGet(() -> getConfigurations().create(names.getConfigurationName("api"),
				builder.asBucket().withDescription("API dependencies for JNI library.")));

		jvmImplementationDependencies = getConfigurations().create("jvmImplementation",
			builder.asBucket(apiDependencies).withDescription("Implementation only dependencies for JNI library."));
		jvmRuntimeOnly = getConfigurations().create("jvmRuntimeOnly",
			builder.asBucket().withDescription("Runtime only dependencies for JNI library."));
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

	public Configuration getJvmRuntimeOnlyDependencies() {
		return jvmRuntimeOnly;
	}
}
