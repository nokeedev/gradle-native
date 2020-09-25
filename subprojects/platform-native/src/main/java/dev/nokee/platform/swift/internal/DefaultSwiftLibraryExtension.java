package dev.nokee.platform.swift.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.swift.SwiftLibraryExtension;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;
import static dev.nokee.utils.ConfigureUtils.setPropertyValue;

public class DefaultSwiftLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements SwiftLibraryExtension, Component {
	@Getter private final ConfigurableFileCollection swiftSources;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultSwiftLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		super(component, objects, providers, layout);
		this.swiftSources = objects.fileCollection();
		this.targetLinkages = configureDisplayName(objects.setProperty(TargetLinkage.class), "targetLinkages");
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");

		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").from(this.getSwiftSources().getElements().map(toIfEmpty("src/main/swift"))));
	}

	public void setTargetMachines(Object value) {
		ConfigureUtils.setPropertyValue(targetMachines, value);
	}

	public void setTargetBuildTypes(Object value) {
		ConfigureUtils.setPropertyValue(targetBuildTypes, value);
	}

	public void setTargetLinkages(Object value) {
		ConfigureUtils.setPropertyValue(targetLinkages, value);
	}

	@Override
	public NativeLibraryComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeLibrary> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeLibrary.class);
	}
}
