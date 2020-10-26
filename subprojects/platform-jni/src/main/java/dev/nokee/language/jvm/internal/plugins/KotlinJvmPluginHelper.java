package dev.nokee.language.jvm.internal.plugins;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.lang.reflect.InvocationTargetException;

public final class KotlinJvmPluginHelper {
	private KotlinJvmPluginHelper() {}

	public static void whenSourceSetKnown(Project project, String name, Action<? super SourceDirectorySet> action) {
		project.getExtensions().getByType(SourceSetContainer.class).configureEach(sourceSet -> {
			if (sourceSet.getName().equals(name)) {
				action.execute(asKotlinSourceSet(sourceSet));
			}
		});
	}

	private static SourceDirectorySet asKotlinSourceSet(SourceSet sourceSet) {
		try {
			val kotlinSourceSet = new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
			val DefaultKotlinSourceSet = kotlinSourceSet.getClass();
			val getKotlin = DefaultKotlinSourceSet.getMethod("getKotlin");
			return (SourceDirectorySet) getKotlin.invoke(kotlinSourceSet);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
