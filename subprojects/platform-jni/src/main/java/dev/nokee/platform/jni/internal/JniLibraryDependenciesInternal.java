package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultDependencyBucket;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DependencyBucket;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Optional;

public abstract class JniLibraryDependenciesInternal implements JniLibraryDependencies {
	private final DependencyBucket apiDependencies;
	private final DependencyBucket jvmImplementationDependencies;
	private final DependencyBucket jvmRuntimeOnly;
	@Getter private final DefaultNativeComponentDependencies nativeDelegate;

	@Inject
	public JniLibraryDependenciesInternal(NamingScheme names, DefaultNativeComponentDependencies nativeDelegate) {
		this.nativeDelegate = nativeDelegate;
		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

		// Kotlin may create this configuration before us
		apiDependencies = getObjects().newInstance(DefaultDependencyBucket.class, Optional.ofNullable(getConfigurations().findByName(names.getConfigurationName("api"))).orElseGet(() -> getConfigurations().create(names.getConfigurationName("api"), builder.asBucket().withDescription("API dependencies for JNI library."))));

		jvmImplementationDependencies = getObjects().newInstance(DefaultDependencyBucket.class, getConfigurations().create(names.getConfigurationName("jvmImplementation"), builder.asBucket(apiDependencies.getAsConfiguration()).withDescription("Implementation only dependencies for JNI library.")));
		jvmRuntimeOnly = getObjects().newInstance(DefaultDependencyBucket.class, getConfigurations().create(names.getConfigurationName("jvmRuntimeOnly"), builder.asBucket().withDescription("Runtime only dependencies for JNI library.")));
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Override
	public void api(Object notation) {
		apiDependencies.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		apiDependencies.addDependency(notation, action);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementationDependencies.addDependency(notation);
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ModuleDependency> action) {
		jvmImplementationDependencies.addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.addDependency(notation);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		jvmRuntimeOnly.addDependency(notation, action);
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeDelegate.implementation(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		nativeDelegate.implementation(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		nativeDelegate.linkOnly(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeDelegate.linkOnly(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		nativeDelegate.runtimeOnly(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeDelegate.runtimeOnly(notation, action);
	}

	public Configuration getApiDependencies() {
		return apiDependencies.getAsConfiguration();
	}

	public Configuration getJvmImplementationDependencies() {
		return jvmImplementationDependencies.getAsConfiguration();
	}

	public Configuration getJvmRuntimeOnlyDependencies() {
		return jvmRuntimeOnly.getAsConfiguration();
	}

	public Configuration getNativeImplementationDependencies() {
		return nativeDelegate.getImplementationDependencies();
	}

	public Configuration getNativeLinkOnlyDependencies() {
		return nativeDelegate.getLinkOnlyDependencies();
	}

	public Configuration getNativeRuntimeOnlyDependencies() {
		return nativeDelegate.getRuntimeOnlyDependencies();
	}
}
