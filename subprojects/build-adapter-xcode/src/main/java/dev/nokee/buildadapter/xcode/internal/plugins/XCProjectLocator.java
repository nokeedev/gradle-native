package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class XCProjectLocator {
	public List<Path> findProjects(Path searchDirectory) {
		if (Files.notExists(searchDirectory)) {
			return ImmutableList.of();
		}

		try (val stream = Files.newDirectoryStream(searchDirectory, this::filterXcodeProject)) {
			return ImmutableList.copyOf(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to locate Xcode project.", e);
		}
	}

	private boolean filterXcodeProject(Path entry) {
		return entry.getFileName().toString().endsWith(".xcodeproj");
	}
}
