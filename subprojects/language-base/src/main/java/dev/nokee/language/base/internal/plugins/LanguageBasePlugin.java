package dev.nokee.language.base.internal.plugins;

import dev.nokee.language.base.internal.KnownLanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetConfigurer;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LanguageBasePlugin implements Plugin<Project> {
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
	}
}
