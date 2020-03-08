package dev.nokee.language.c.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("c");
	}
}
