package dev.nokee.language.base.internal.plugins;

import dev.nokee.language.base.internal.*;
import dev.nokee.language.base.internal.rules.LanguageSourceSetConventionRule;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class LanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public LanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val languageSourceSetRepository = new LanguageSourceSetRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(LanguageSourceSetRepository.class, "__NOKEE_languageSourceSetRepository", languageSourceSetRepository);

		val languageSourceSetConfigurer = new LanguageSourceSetConfigurer(eventPublisher);
		project.getExtensions().add(LanguageSourceSetConfigurer.class, "__NOKEE_languageSourceSetConfigurer", languageSourceSetConfigurer);

		val knownLanguageSourceSetFactory = new KnownLanguageSourceSetFactory(() -> languageSourceSetRepository, () -> project.getExtensions().getByType(LanguageSourceSetConfigurer.class));
		project.getExtensions().add(KnownLanguageSourceSetFactory.class, "__NOKEE_knownLanguageSourceSetFactory", knownLanguageSourceSetFactory);

		val languageSourceSetViewFactory = new LanguageSourceSetViewFactory(languageSourceSetRepository, languageSourceSetConfigurer, knownLanguageSourceSetFactory);
		project.getExtensions().add(LanguageSourceSetViewFactory.class, "__NOKEE_languageSourceSetViewFactory", languageSourceSetViewFactory);

		val languageSourceSetInstantiator = new LanguageSourceSetInstantiatorImpl("project language source set instantiator");
		project.getExtensions().add(LanguageSourceSetInstantiator.class, "__NOKEE_languageSourceSetInstantiator", languageSourceSetInstantiator);

		languageSourceSetInstantiator.registerFactory(LanguageSourceSetImpl.class, this::newSourceSet);

		val languageSourceSetRegistry = new LanguageSourceSetRegistry(eventPublisher, languageSourceSetInstantiator);
		project.getExtensions().add(LanguageSourceSetRegistry.class, "__NOKEE_languageSourceSetRegistry", languageSourceSetRegistry);

		languageSourceSetConfigurer.configureEach(ProjectIdentifier.of(project), LanguageSourceSetInternal.class, new LanguageSourceSetConventionRule(project.getObjects()));
	}

	private LanguageSourceSetImpl newSourceSet(DomainObjectIdentifier identifier) {
		return new LanguageSourceSetImpl((LanguageSourceSetIdentifier<?>) identifier, objectFactory);
	}
}
