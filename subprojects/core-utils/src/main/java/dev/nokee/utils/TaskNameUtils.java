package dev.nokee.utils;

public final class TaskNameUtils {
	public static String getShortestName(String taskName) {
		return taskName
			.chars()
			.filter(it -> !Character.isLowerCase(it))
			.collect(() -> new StringBuilder().appendCodePoint(taskName.codePointAt(0)), StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}
}
