package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.internal.*;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public ObjectiveCLanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val languageSourceSetInstantiator = project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
		languageSourceSetInstantiator.registerFactory(ObjectiveCSourceSetImpl.class, this::newObjectiveCSourceSet);
		languageSourceSetInstantiator.registerFactoryIfAbsent(CHeaderSetImpl.class, this::newCHeaderSet);

		val languageSourceSetConfigurer = project.getExtensions().getByType(LanguageSourceSetConfigurer.class);
		languageSourceSetConfigurer.configureEach(ProjectIdentifier.of(project), ObjectiveCSourceSetImpl.class, this::configureConvention);
	}

	private void configureConvention(LanguageSourceSetInternal sourceSet) {
		if (sourceSet.getIdentifier().getName().get().equals("objectiveC")) {
			sourceSet.convention(objectFactory.fileCollection().from(ConventionalRelativeLanguageSourceSetPath.of(sourceSet.getIdentifier()), ConventionalRelativeLanguageSourceSetPath.builder().fromIdentifier(sourceSet.getIdentifier()).withSourceSetName("objc").build()));
		}
	}

	private ObjectiveCSourceSetImpl newObjectiveCSourceSet(DomainObjectIdentifier identifier) {
		return new ObjectiveCSourceSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}

	private CHeaderSetImpl newCHeaderSet(DomainObjectIdentifier identifier) {
		return new CHeaderSetImpl((LanguageSourceSetIdentifier<?>) identifier, objectFactory);
	}
}
