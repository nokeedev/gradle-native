package dev.gradleplugins.grava.util;

import org.gradle.api.invocation.Gradle;

public final class GradleUtils {
	private GradleUtils() {}

	public static boolean isHostBuild(Gradle gradle) {
		return gradle.getParent() == null;
	}

	public static boolean hasIncludedBuilds(Gradle gradle) {
		return !gradle.getIncludedBuilds().isEmpty();
	}

	public static boolean isIncludedBuild(Gradle gradle) {
		return gradle.getParent() != null;
	}

	public static boolean isCompositeBuild(Gradle gradle) {
		return isIncludedBuild(gradle) || hasIncludedBuilds(gradle);
	}
}
