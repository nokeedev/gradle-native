package dev.nokee.language.jvm.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet;

public final class KotlinJvmPluginHelper {
	private KotlinJvmPluginHelper() {}

	public static void whenSourceSetKnown(Project project, String name, Action<? super SourceDirectorySet> action) {
		project.getExtensions().getByType(SourceSetContainer.class).configureEach(sourceSet -> {
			if (sourceSet.getName().equals(name)) {
				action.execute(asKotlinSourceSet(sourceSet).getKotlin());
			}
		});
	}

	private static KotlinSourceSet asKotlinSourceSet(SourceSet sourceSet) {
		return (KotlinSourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
	}
}
