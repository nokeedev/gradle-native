package dev.nokee.language.jvm.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSetContainer;

public final class JavaJvmPluginHelper {
	private JavaJvmPluginHelper() {}

	public static void whenSourceSetKnown(Project project, String name, Action<? super SourceDirectorySet> action) {
		project.getExtensions().getByType(SourceSetContainer.class).configureEach(sourceSet -> {
			if (sourceSet.getName().equals(name)) {
				action.execute(sourceSet.getJava());
			}
		});
	}
}
