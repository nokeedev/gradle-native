package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class JniLibraryExtensionInternal implements JniLibraryExtension, Component {
	@Getter private final JniLibraryComponentInternal component;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter private final LanguageSourceSetView<LanguageSourceSet> sources;
	@Getter private final VariantViewInternal<JniLibrary> variants;

	@Inject
	public JniLibraryExtensionInternal(JniLibraryComponentInternal component, ConfigurationContainer configurations, ObjectFactory objects, ProviderFactory providers, VariantViewFactory variantViewFactory) {
		this.configurations = configurations;
		this.objects = objects;
		this.providers = providers;
		this.component = component;
		this.sources = component.getSources();
		this.variants = variantViewFactory.create(component.getIdentifier(), JniLibrary.class);
	}

	//region Variant-awareness
	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		return component.getVariantCollection();
	}

	public SetProperty<DimensionType> getDimensions() {
		return component.getDimensions();
	}

	public void setDimensions(Object value) {
		ConfigureUtils.setPropertyValue(component.getDimensions(), value);
	}

	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return component.getBuildVariants();
	}

	public void setBuildVariants(Object value) {
		ConfigureUtils.setPropertyValue(component.getBuildVariants(), value);
	}
	//endregion

	@Override
	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}

	public Configuration getJvmImplementationDependencies() {
		return component.getJvmImplementationDependencies();
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return component.getDependencies();
	}

	@Override
	public TargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	public SetProperty<TargetMachine> getTargetMachines() {
		return component.getTargetMachines();
	}

	public void setTargetMachines(Object value) {
		ConfigureUtils.setPropertyValue(component.getTargetMachines(), value);
	}
}
