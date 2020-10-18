package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;
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

public class DefaultObjectiveCppApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements ObjectiveCppApplicationExtension, Component {
	@Getter private final ObjectiveCppSourceSet objectiveCppSources;
	@Getter private final CppHeaderSet privateHeaders;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	@Getter private final SetProperty<TargetBuildType> targetBuildTypes;
	@Getter private final LanguageSourceSetView<LanguageSourceSet> sources;

	@Inject
	public DefaultObjectiveCppApplicationExtension(DefaultNativeApplicationComponent component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers, layout);
		this.objectiveCppSources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("objcpp"), ObjectiveCppSourceSetImpl.class, component.getIdentifier()));
		this.privateHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("headers"), CppHeaderSetImpl.class, component.getIdentifier()));
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

	@Override
	public void objectiveCppSources(Action<? super ObjectiveCppSourceSet> action) {
		action.execute(objectiveCppSources);
	}

	@Override
	public void privateHeaders(Action<? super CppHeaderSet> action) {
		action.execute(privateHeaders);
	}
}
