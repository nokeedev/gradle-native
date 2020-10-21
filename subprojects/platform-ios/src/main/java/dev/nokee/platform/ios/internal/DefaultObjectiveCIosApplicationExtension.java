package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.language.base.internal.*;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.HasLanguageSourceSetAccessor;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

public class DefaultObjectiveCIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements ObjectiveCIosApplicationExtension, Component, HasLanguageSourceSetAccessor {
	@Getter private final ObjectiveCSourceSet objectiveCSources;
	@Getter private final CHeaderSet privateHeaders;
	@Getter private final IosResourceSet resources;
	@Getter private final LanguageSourceSetView<LanguageSourceSet> sources;
	private final ObjectFactory objectFactory;

	public DefaultObjectiveCIosApplicationExtension(DefaultIosApplicationComponent component, ObjectFactory objects, ProviderFactory providers, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers);
		this.objectFactory = objects;
		this.sources = component.getSources();
		this.objectiveCSources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("objectiveC"), ObjectiveCSourceSetImpl.class, component.getIdentifier()), this::configureSourceSet);
		this.privateHeaders = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("headers"), CHeaderSetImpl.class, component.getIdentifier()));
		this.resources = languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("resources"), IosResourceSetImpl.class, component.getIdentifier()));
	}

	private void configureSourceSet(LanguageSourceSetInternal sourceSet) {
		sourceSet.convention(objectFactory.fileCollection().from(ConventionalRelativeLanguageSourceSetPath.of(sourceSet.getIdentifier()), ConventionalRelativeLanguageSourceSetPath.builder().fromIdentifier(sourceSet.getIdentifier()).withSourceSetName("objc").build()));
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<IosApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(IosApplication.class);
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
	public void resources(Action<? super IosResourceSet> action) {
		action.execute(resources);
	}
}
