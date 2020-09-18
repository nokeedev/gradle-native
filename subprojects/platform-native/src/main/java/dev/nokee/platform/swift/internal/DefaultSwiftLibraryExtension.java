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
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class DefaultSwiftLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements SwiftLibraryExtension, Component {
	@Getter private final ConfigurableFileCollection sources;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultSwiftLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		super(component, objects, providers, layout);
		this.sources = objects.fileCollection();
		this.targetLinkages = objects.setProperty(TargetLinkage.class);
		this.targetMachines = objects.setProperty(TargetMachine.class);
		this.targetBuildTypes = objects.setProperty(TargetBuildType.class);

		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").from(getSources().getElements().map(toIfEmpty("src/main/swift"))));
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
