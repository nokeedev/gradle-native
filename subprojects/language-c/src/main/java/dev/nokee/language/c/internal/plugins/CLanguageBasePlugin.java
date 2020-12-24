package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(NativeHeaderSet.class)
			.defaultImport(CHeaderSet.class)
			.defaultImport(CSourceSet.class);

		// No need to register anything as CHeaderSet and CSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.
	}
}
