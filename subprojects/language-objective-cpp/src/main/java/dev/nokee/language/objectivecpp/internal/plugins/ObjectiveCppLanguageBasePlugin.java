package dev.nokee.language.objectivecpp.internal.plugins;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.language.base.internal.LanguageModule;
import dev.nokee.language.base.internal.rules.RegisterLanguageFactoriesRule;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppLanguageModule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

public class ObjectiveCppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		DaggerObjectiveCppLanguageBasePlugin_RuleSource.factory().create(project).registerFactories().run();
	}

	@Component(modules = {LanguageModule.class, ObjectiveCppLanguageModule.class, GradleModule.class})
	interface RuleSource {
		RegisterLanguageFactoriesRule registerFactories();

		@Component.Factory
		interface Factory {
			RuleSource create(@BindsInstance Project project);
		}
	}
}
