package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
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
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final DomainObjectSet<JniLibraryInternal> variantCollection;

	@Inject
	public JniLibraryExtensionInternal(JniLibraryDependenciesInternal dependencies, GroupId groupId) {
		binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		variantCollection = getObjects().domainObjectSet(JniLibraryInternal.class);
		this.dependencies = dependencies;
		this.groupId = groupId;
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	public DomainObjectSet<JniLibraryInternal> getVariantCollection() {
		return variantCollection;
	}

	public JniLibraryInternal newVariant(NamingScheme names, TargetMachine targetMachine) {
		return getObjects().newInstance(JniLibraryInternal.class, names, sources, dependencies.getNativeDependencies(), targetMachine, groupId, binaryCollection);
	}

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, binaryCollection));
	}

	public VariantView<JniLibrary> getVariants() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultVariantView.class, variantCollection));
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
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
