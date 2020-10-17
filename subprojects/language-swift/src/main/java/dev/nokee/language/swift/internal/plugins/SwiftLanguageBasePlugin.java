package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.swift.internal.SwiftSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public SwiftLanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val languageSourceSetInstantiator = project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
		languageSourceSetInstantiator.registerFactory(SwiftSourceSetImpl.class, this::newSwiftSourceSet);
	}

	private SwiftSourceSetImpl newSwiftSourceSet(DomainObjectIdentifier identifier) {
		return new SwiftSourceSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}
}
