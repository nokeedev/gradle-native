package dev.nokee.platform.c.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.c.internal.CSourceSetImpl;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.runtime.nativebase.TargetBuildType;
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

public class DefaultCApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements CApplicationExtension, Component {
	private final CSourceSet cSources;
	@Getter private final CHeaderSet privateHeaders;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;

	@Inject
	public DefaultCApplicationExtension(DefaultNativeApplicationComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers, layout);
		this.cSources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("c"), CSourceSetImpl.class, component.getIdentifier()));
		this.privateHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("headers"), CHeaderSetImpl.class, component.getIdentifier()));
		this.targetMachines = configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.targetBuildTypes = configureDisplayName(objects.setProperty(TargetBuildType.class), "targetBuildTypes");
	}

	public void setTargetMachines(Object value) {
		ConfigureUtils.setPropertyValue(targetMachines, value);
	}

	public void setTargetBuildTypes(Object value) {
		ConfigureUtils.setPropertyValue(targetBuildTypes, value);
	}

	@Override
	public CSourceSet getCSources() {
		return cSources;
	}

	public CSourceSet getcSources() {
		return cSources;
	}

	@Override
	public void cSources(Action<? super CSourceSet> action) {
		action.execute(cSources);
	}

	@Override
	public void privateHeaders(Action<? super CHeaderSet> action) {
		action.execute(privateHeaders);
	}

	@Override
	public NativeApplicationComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeApplication.class);
	}
}
