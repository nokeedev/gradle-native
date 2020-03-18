package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.base.internal.DefaultVariantView;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Cast;

import javax.inject.Inject;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final ConfigurationContainer configurations;
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<? super BinaryInternal> binaries;

	public DomainObjectSet<JniLibraryInternal> getVariantCollection() {
		return variantCollection;
	}

	private final DomainObjectSet<JniLibraryInternal> variantCollection;

	@Inject
	public JniLibraryExtensionInternal(ObjectFactory objectFactory, ConfigurationContainer configurations, JniLibraryDependenciesInternal dependencies, GroupId groupId) {
		binaries = objectFactory.domainObjectSet(BinaryInternal.class);
		sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
		variantCollection = objectFactory.domainObjectSet(JniLibraryInternal.class);
		this.configurations = configurations;
		this.dependencies = dependencies;
		this.groupId = groupId;
	}

	@Inject
	protected abstract ObjectFactory getObjectFactory();

	public JniLibraryInternal newVariant(NamingScheme names, TargetMachine targetMachine) {
		return getObjectFactory().newInstance(JniLibraryInternal.class, names, configurations, sources, dependencies.getNativeDependencies(), targetMachine, groupId);
	}

	public DomainObjectSet<? super BinaryInternal> getBinaries() {
		return binaries;
	}

	public VariantView<? extends JniLibraryInternal> getVariants() {
		return Cast.uncheckedCast(getObjectFactory().newInstance(DefaultVariantView.class, variantCollection));
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

	@Override
	public abstract SetProperty<TargetMachine> getTargetMachines();
}
