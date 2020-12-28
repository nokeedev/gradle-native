package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCSourceSet.class)
			.defaultImport(CHeaderSet.class)
			.defaultImport(NativeHeaderSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.
	}
}
