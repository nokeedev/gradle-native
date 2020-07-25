package dev.nokee.utils;

import org.gradle.api.invocation.Gradle;

public final class GradleUtils {
	private GradleUtils() {}

	public static boolean isHostBuild(Gradle gradle) {
		return gradle.getParent() == null;
	}
}
