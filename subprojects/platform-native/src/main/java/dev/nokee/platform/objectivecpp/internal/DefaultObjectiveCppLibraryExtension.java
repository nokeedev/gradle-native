package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class DefaultObjectiveCppLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements ObjectiveCppLibraryExtension {
	private final ObjectiveCppLibraryComponentSources componentSources;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultObjectiveCppLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, ObjectiveCppLibraryComponentSources componentSources) {
		super(component, objects, providers, layout);
		this.targetLinkages = objects.setProperty(TargetLinkage.class);
		this.targetMachines = objects.setProperty(TargetMachine.class);
		this.targetBuildTypes = objects.setProperty(TargetBuildType.class);
		this.componentSources = componentSources;

		// Shimming both component sources for now...
		getComponent().getSourceCollection().addAll(componentSources.getAsView().get());
	}

	@Override
	public NativeLibraryComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeLibrary> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeLibrary.class);
	}

	@Override
	public ObjectiveCppSourceSet getObjectiveCppSources() {
		return componentSources.getObjectiveCppSources();
	}

	@Override
	public CppHeaderSet getPrivateHeaders() {
		return componentSources.getPrivateHeaders();
	}

	@Override
	public CppHeaderSet getPublicHeaders() {
		return componentSources.getPublicHeaders();
	}

	@Override
	public SourceView<LanguageSourceSet> getSources() {
		return componentSources.getAsView();
	}
}
