package dev.nokee.internal;

import org.gradle.api.Project;

public final class ProjectUtils {
	private ProjectUtils() {}

	public static boolean isRootProject(Project project) {
		return project.getParent() == null;
	}

	public static String getPrefixableProjectPath(Project project) {
		if (isRootProject(project)) {
			return "";
		}
		return project.getPath();
	}
}
