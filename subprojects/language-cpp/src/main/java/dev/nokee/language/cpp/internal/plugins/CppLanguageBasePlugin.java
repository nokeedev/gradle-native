package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.cpp.internal.CppSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class CppLanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public CppLanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val languageSourceSetInstantiator = project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
		languageSourceSetInstantiator.registerFactory(CppSourceSetImpl.class, this::newCppSourceSet);
		languageSourceSetInstantiator.registerFactoryIfAbsent(CppHeaderSetImpl.class, this::newCppHeaderSet);
	}

	private CppHeaderSetImpl newCppHeaderSet(DomainObjectIdentifier identifier) {
		return new CppHeaderSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}

	private CppSourceSetImpl newCppSourceSet(DomainObjectIdentifier identifier) {
		return new CppSourceSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}
}
