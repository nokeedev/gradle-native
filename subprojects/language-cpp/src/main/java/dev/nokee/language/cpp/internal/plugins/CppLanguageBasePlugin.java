package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(NativeHeaderSet.class)
			.defaultImport(CppHeaderSet.class)
			.defaultImport(CppSourceSet.class);

		// No need to register anything as CppHeaderSet and CppSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.
	}
}
