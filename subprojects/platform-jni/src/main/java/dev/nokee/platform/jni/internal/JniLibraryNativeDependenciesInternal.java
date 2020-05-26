package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.runtime.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.DependencyBucket;
import dev.nokee.runtime.nativebase.internal.NativeDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

// TODO: Add tests for linkOnly and runtimeOnly
// TODO: Add tests for per variant dependencies
public abstract class JniLibraryNativeDependenciesInternal implements JniLibraryNativeDependencies {
	private final DependencyBucket nativeImplementationDependencies;
	private final DependencyBucket nativeLinkOnly;
	private final DependencyBucket nativeRuntimeOnly;

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	public JniLibraryNativeDependenciesInternal(NamingScheme names) {
		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);
		nativeImplementationDependencies = getObjects().newInstance(NativeDependencyBucket.class,
			getConfigurations().create(names.getConfigurationName("nativeImplementation"), builder.asBucket().withDescription("Implementation only dependencies for JNI shared library.")));
		nativeLinkOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("nativeLinkOnly"),
			builder.asBucket().withDescription("Link only dependencies for JNI shared library.")));
		nativeRuntimeOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("nativeRuntimeOnly"),
			builder.asBucket().withDescription("Runtime only dependencies for JNI shared library.")));
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeImplementationDependencies.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void nativeImplementation(Object notation, Action<? super T> action) {
		nativeImplementationDependencies.addDependency(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		nativeLinkOnly.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void nativeLinkOnly(Object notation, Action<? super T> action) {
		nativeLinkOnly.addDependency(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		nativeRuntimeOnly.addDependency(notation);
	}

	@Override
	public <T extends ModuleDependency> void nativeRuntimeOnly(Object notation, Action<? super T> action) {
		nativeRuntimeOnly.addDependency(notation, action);
	}

	public Configuration getNativeDependencies() {
		return nativeImplementationDependencies.getAsConfiguration();
	}

	public Configuration getNativeLinkOnlyDependencies() {
		return nativeLinkOnly.getAsConfiguration();
	}

	public Configuration getNativeRuntimeOnlyDependencies() {
		return nativeRuntimeOnly.getAsConfiguration();
	}

	public JniLibraryNativeDependenciesInternal extendsFrom(JniLibraryNativeDependenciesInternal dependencies) {
		getNativeDependencies().extendsFrom(dependencies.getNativeDependencies());
		getNativeLinkOnlyDependencies().extendsFrom(dependencies.getNativeLinkOnlyDependencies());
		getNativeRuntimeOnlyDependencies().extendsFrom(dependencies.getNativeRuntimeOnlyDependencies());
		return this;
	}
}
