package dev.nokee.language.base.internal;

import dagger.Module;
import dagger.Provides;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import org.gradle.api.Project;

@Module
public interface LanguageModule {
	@Provides
	static LanguageSourceSetFactoryRegistry aLanguageRegistry(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);
		return project.getExtensions().getByType(LanguageSourceSetFactoryRegistry.class);
	}

	@Provides
	static LanguageSourceSetInstantiator aLanguageInstantiator(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);
		return project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
	}
}
