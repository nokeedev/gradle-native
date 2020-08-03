package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class XCWorkspaceLocator {
	public List<Path> findWorkspaces(Path searchDirectory) {
		if (Files.notExists(searchDirectory)) {
			return ImmutableList.of();
		}

		try (val stream = Files.newDirectoryStream(searchDirectory, this::filterXcodeWorkspace)) {
			return ImmutableList.copyOf(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to locate Xcode workspace.", e);
		}
	}

	private boolean filterXcodeWorkspace(Path entry) {
		return entry.getFileName().toString().endsWith(".xcworkspace");
	}
}
