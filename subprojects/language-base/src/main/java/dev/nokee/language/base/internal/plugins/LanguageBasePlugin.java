package dev.nokee.language.base.internal.plugins;

import dev.nokee.language.base.LanguageSourceSetFactoryRegistry;
import dev.nokee.language.base.LanguageSourceSetInstantiator;
import dev.nokee.language.base.internal.LanguageSourceSetInstantiatorImpl;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val instantiator = new LanguageSourceSetInstantiatorImpl();
		project.getExtensions().add(LanguageSourceSetFactoryRegistry.class, "__languageRegistry_", instantiator);
		project.getExtensions().add(LanguageSourceSetInstantiator.class, "__languageInstantiator_", instantiator);
	}
}
