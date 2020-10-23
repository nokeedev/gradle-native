package dev.nokee.platform.base.internal.plugins;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ComponentModelBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);
		project.getPluginManager().apply(VariantBasePlugin.class);
		project.getPluginManager().apply(LanguageBasePlugin.class);
	}
}
