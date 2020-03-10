package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.jni.JniLibraryExtension;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final ConfigurationContainer configurations;
	private final JniLibraryDependenciesInternal dependencies;
	private final DomainObjectSet<? super BinaryInternal> binaries;
	private final DomainObjectSet<JniLibraryInternal> variants;

	@Inject
	public JniLibraryExtensionInternal(ObjectFactory objectFactory, ConfigurationContainer configurations, JniLibraryDependenciesInternal dependencies) {
		binaries = objectFactory.domainObjectSet(BinaryInternal.class);
		sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
		variants = objectFactory.domainObjectSet(JniLibraryInternal.class);
		this.configurations = configurations;
		this.dependencies = dependencies;
	}

	@Inject
	protected abstract ObjectFactory getObjectFactory();

	public JniLibraryInternal newVariant() {
		return getObjectFactory().newInstance(JniLibraryInternal.class, configurations, sources, dependencies.getNativeDependencies());
	}

	public DomainObjectSet<? super BinaryInternal> getBinaries() {
		return binaries;
	}

	public DomainObjectSet<JniLibraryInternal> getVariants() {
		return variants;
	}

	public DomainObjectSet<? super LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public Configuration getNativeImplementationDependencies() {
		return dependencies.getNativeDependencies();
	}

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmDependencies();
	}

	@Override
	public JniLibraryDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super JniLibraryDependencies> action) {
		action.execute(dependencies);
	}
}
