package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	@Getter private final JniLibraryComponentInternal component;

	@Inject
	public JniLibraryExtensionInternal(GroupId groupId, NamingScheme names) {
		this.component = getObjects().newInstance(JniLibraryComponentInternal.class, names, groupId);
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	//region Variant-awareness
	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		return component.getVariantCollection();
	}

	@Override
	public VariantView<JniLibrary> getVariants() {
		return component.getVariants();
	}

	public SetProperty<DimensionType> getDimensions() {
		return component.getDimensions();
	}

	public SetProperty<BuildVariant> getBuildVariants() {
		return component.getBuildVariants();
	}
	//endregion

	@Override
	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return component.getSources();
	}

	public Configuration getJvmImplementationDependencies() {
		return component.getJvmImplementationDependencies();
	}

	@Override
	public JniLibraryDependenciesInternal getDependencies() {
		return component.getDependencies();
	}

	@Override
	public void dependencies(Action<? super JniLibraryDependencies> action) {
		component.dependencies(action);
	}

	@Override
	public TargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	public SetProperty<TargetMachine> getTargetMachines() {
		return component.getTargetMachines();
	}
}
