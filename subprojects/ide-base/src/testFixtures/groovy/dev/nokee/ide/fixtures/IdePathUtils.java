package dev.nokee.ide.fixtures;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public final class IdePathUtils {
	private IdePathUtils() {}

	public static String addExtensionIfAbsent(Object anyPath, String extension) {
		if (anyPath instanceof File) {
			anyPath = ((File) anyPath).getAbsolutePath();
		}
		String path = anyPath.toString();
		if (!FilenameUtils.isExtension(path, extension)) {
			path = path + "." + extension;
		}
		return path;
	}
}
