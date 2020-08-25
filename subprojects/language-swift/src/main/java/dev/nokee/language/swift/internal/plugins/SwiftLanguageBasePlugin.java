package dev.nokee.language.swift.internal.plugins;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.language.base.internal.LanguageModule;
import dev.nokee.language.base.internal.rules.RegisterLanguageFactoriesRule;
import dev.nokee.language.swift.internal.SwiftLanguageModule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		DaggerSwiftLanguageBasePlugin_RuleSource.factory().create(project).registerFactories().run();
	}

	@Component(modules = {LanguageModule.class, SwiftLanguageModule.class, GradleModule.class})
	interface RuleSource {
		RegisterLanguageFactoriesRule registerFactories();

		@Component.Factory
		interface Factory {
			RuleSource create(@BindsInstance Project project);
		}
	}
}
