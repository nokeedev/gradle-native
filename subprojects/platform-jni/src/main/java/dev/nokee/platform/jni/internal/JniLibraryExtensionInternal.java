package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class JniLibraryExtensionInternal implements JniLibraryExtension {
	@Getter private final JniLibraryComponentInternal component;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	@Inject
	public JniLibraryExtensionInternal(GroupId groupId, NamingScheme names, ConfigurationContainer configurations, ObjectFactory objects, ProviderFactory providers, ComponentSourcesInternal componentSources) {
		this.configurations = configurations;
		this.objects = objects;
		this.providers = providers;
		this.component = objects.newInstance(JniLibraryComponentInternal.class, names, groupId, componentSources);
	}

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

	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return component.getBuildVariants();
	}
	//endregion

	@Override
	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}

	public SourceView<LanguageSourceSet> getSources() {
		return component.getComponentSources().getAsView();
	}

	public Configuration getJvmImplementationDependencies() {
		return component.getJvmImplementationDependencies();
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return component.getDependencies();
	}

	@Override
	public void dependencies(Action<? super JavaNativeInterfaceLibraryComponentDependencies> action) {
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
