package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.DefaultDependencyBucket;
import dev.nokee.platform.nativebase.internal.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;
import java.util.Optional;

// TODO: Add tests for jvmRuntimeOnly
public abstract class JniLibraryDependenciesInternal extends JniLibraryNativeDependenciesInternal implements JniLibraryDependencies {
	private final DependencyBucket apiDependencies;
	private final DependencyBucket jvmImplementationDependencies;
	private final DependencyBucket jvmRuntimeOnly;

	@Inject
	public JniLibraryDependenciesInternal(NamingScheme names) {
		super(names);
		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

		// Kotlin may create this configuration before us
		apiDependencies = getObjects().newInstance(DefaultDependencyBucket.class, Optional.ofNullable(getConfigurations().findByName(names.getConfigurationName("api"))).orElseGet(() -> getConfigurations().create(names.getConfigurationName("api"), builder.asBucket().withDescription("API dependencies for JNI library."))));

		jvmImplementationDependencies = getObjects().newInstance(DefaultDependencyBucket.class, getConfigurations().create("jvmImplementation", builder.asBucket(apiDependencies.getAsConfiguration()).withDescription("Implementation only dependencies for JNI library.")));
		jvmRuntimeOnly = getObjects().newInstance(DefaultDependencyBucket.class, getConfigurations().create("jvmRuntimeOnly", builder.asBucket().withDescription("Runtime only dependencies for JNI library.")));
	}

	@Override
	public void api(Object notation) {
		apiDependencies.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void api(Object notation, Action<? super T> action) {
		apiDependencies.addDependency(notation, action);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementationDependencies.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void jvmImplementation(Object notation, Action<? super T> action) {
		jvmImplementationDependencies.addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void jvmRuntimeOnly(Object notation, Action<? super T> action) {
		jvmRuntimeOnly.addDependency(notation, action);
	}

	public Configuration getApiDependencies() {
		return apiDependencies.getAsConfiguration();
	}

	public Configuration getJvmDependencies() {
		return jvmImplementationDependencies.getAsConfiguration();
	}

	public Configuration getJvmRuntimeOnlyDependencies() {
		return jvmRuntimeOnly.getAsConfiguration();
	}
}
