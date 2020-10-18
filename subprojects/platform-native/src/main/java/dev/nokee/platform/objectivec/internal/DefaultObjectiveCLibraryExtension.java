package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;
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

public class DefaultObjectiveCLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements ObjectiveCLibraryExtension, Component {
	@Getter private final ObjectiveCSourceSet objectiveCSources;
	@Getter private final CHeaderSet privateHeaders;
	@Getter private final CHeaderSet publicHeaders;
	@Getter private final SetProperty<TargetLinkage> targetLinkages;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultObjectiveCLibraryExtension(DefaultNativeLibraryComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers, layout);
		this.objectiveCSources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("objc"), ObjectiveCSourceSetImpl.class, component.getIdentifier()));
		this.privateHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("headers"), CHeaderSetImpl.class, component.getIdentifier()));
		this.publicHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("public"), CHeaderSetImpl.class, component.getIdentifier()));
		this.targetLinkages = configureDisplayName(objects.setProperty(TargetLinkage.class), "targetLinkages");
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");
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
	public void objectiveCSources(Action<? super ObjectiveCSourceSet> action) {
		action.execute(objectiveCSources);
	}

	@Override
	public void privateHeaders(Action<? super CHeaderSet> action) {
		action.execute(privateHeaders);
	}

	@Override
	public void publicHeaders(Action<? super CHeaderSet> action) {
		action.execute(publicHeaders);
	}
}
