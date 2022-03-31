package dev.nokee.xcode;

import dev.nokee.xcode.internal.XCWorkspaceDataReader;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

final class XCWorkspace {
	public static XCWorkspace open(Path workspaceLocation) {
//		if (workspaceLocation.exists() && workspaceLocation.isDirectory() && new File(workspaceLocation, "contents.xcworkspacedata").exists() && new File(workspaceLocation, "contents.xcworkspacedata").isFile()) {
		return new XCWorkspace(workspaceLocation);
//		}
//		throw new IllegalArgumentException("Invalid workspace");
	}

	private final XCWorkspaceLayout layout;

	public XCWorkspace(Path workspaceLocation) {
		this.layout = new XCWorkspaceLayout(workspaceLocation);
	}

	public Path getLocation() {
		return layout.getLocation();
	}

	public List<Path> getProjectLocations() {
		try {
			val workspace = new XCWorkspaceDataReader(Files.newBufferedReader(layout.getContentFile(), UTF_8)).read();
			val resolver = new XCFileReferenceResolver(layout.getBaseDirectory().toFile());
			return workspace.getFileRefs().stream().map(resolver::resolve).map(File::toPath).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
