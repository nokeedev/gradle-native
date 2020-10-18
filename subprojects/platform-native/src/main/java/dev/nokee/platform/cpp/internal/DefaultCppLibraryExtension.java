package dev.nokee.platform.cpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.cpp.internal.CppSourceSetImpl;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public class DefaultCppLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements CppLibraryExtension, Component {
	@Getter private final CppSourceSet cppSources;
	@Getter private final CppHeaderSet privateHeaders;
	@Getter private final CppHeaderSet publicHeaders;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;
	@Getter private final LanguageSourceSetView<LanguageSourceSet> sources;

	@Inject
	public DefaultCppLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers, layout);
		this.cppSources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("cpp"), CppSourceSetImpl.class, component.getIdentifier()));
		this.privateHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("headers"), CppHeaderSetImpl.class, component.getIdentifier()));
		this.publicHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("public"), CppHeaderSetImpl.class, component.getIdentifier()));
		this.targetLinkages = configureDisplayName(objects.setProperty(TargetLinkage.class), "targetLinkages");
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");
		this.sources = component.getSources();
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

	@Override
	public void cppSources(Action<? super CppSourceSet> action) {
		action.execute(cppSources);
	}

	@Override
	public void privateHeaders(Action<? super CppHeaderSet> action) {
		action.execute(privateHeaders);
	}

	@Override
	public void publicHeaders(Action<? super CppHeaderSet> action) {
		action.execute(publicHeaders);
	}
}
