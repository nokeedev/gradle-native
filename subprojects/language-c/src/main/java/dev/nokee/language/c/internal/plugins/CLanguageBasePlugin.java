package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.c.internal.CSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class CLanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public CLanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val languageSourceSetInstantiator = project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
		languageSourceSetInstantiator.registerFactory(CSourceSetImpl.class, this::newCSourceSet);
		languageSourceSetInstantiator.registerFactoryIfAbsent(CHeaderSetImpl.class, this::newCHeaderSet);
	}

	private CHeaderSetImpl newCHeaderSet(DomainObjectIdentifier identifier) {
		return new CHeaderSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}

	private CSourceSetImpl newCSourceSet(DomainObjectIdentifier identifier) {
		return new CSourceSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}
}
