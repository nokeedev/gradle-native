package dev.nokee.language.jvm.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public final class GroovyJvmPluginHelper {
	private GroovyJvmPluginHelper() {}

	public static void whenSourceSetKnown(Project project, String name, Action<? super SourceDirectorySet> action) {
		project.getExtensions().getByType(SourceSetContainer.class).configureEach(sourceSet -> {
			if (sourceSet.getName().equals(name)) {
				action.execute(asGroovySourceSet(sourceSet).getGroovy());
			}
		});
	}

	private static GroovySourceSet asGroovySourceSet(SourceSet sourceSet) {
		return (GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy");
	}
}
