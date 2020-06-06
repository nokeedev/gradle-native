package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.*;
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
		ConfigurationUtils builder = getObjects().newInstance(ConfigurationUtils.class);

		this.implementation = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("implementation"),
			builder.asBucket().withDescription(names.getConfigurationDescription("Implementation only dependencies for %s."))));

		// HACK: For JNI, needs to clean this up
		String compileOnlyName = names.getConfigurationName("compileOnly");
		if (compileOnlyName.contains("native") || compileOnlyName.contains("Native")) {
			this.compileOnly = null;
		} else {
			this.compileOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(compileOnlyName,
				builder.asBucket().withDescription(names.getConfigurationDescription("Compile only dependencies for %s."))));
		}
		this.linkOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("linkOnly"),
			builder.asBucket().withDescription(names.getConfigurationDescription("Link only dependencies for %s."))));
		this.runtimeOnly = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("runtimeOnly"),
			builder.asBucket().withDescription(names.getConfigurationDescription("Runtime only dependencies for %s."))));
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
		// HACK: For JNI, needs to clean this up
		if (compileOnly == null) {
			return null;
		}
		return compileOnly.getAsConfiguration();
	}

	public Configuration getLinkOnlyDependencies() {
		return linkOnly.getAsConfiguration();
	}

	public Configuration getRuntimeOnlyDependencies() {
		return runtimeOnly.getAsConfiguration();
	}

	public abstract AbstractNativeComponentDependencies extendsWith(NamingScheme names);

	// TODO: It doesn't make sense to have this here. It is shared with JNI platform but only used by native platform
	public abstract AbstractBinaryAwareNativeComponentDependencies newVariantDependency(NamingScheme names, BuildVariant buildVariant, boolean hasSwift);
}
