package dev.nokee.xcode;

import lombok.Getter;

import java.nio.file.Path;

class XCWorkspaceLayout {
	@Getter private final Path location;

	public XCWorkspaceLayout(Path location) {
		this.location = location;
	}

	public Path getContentFile() {
		return location.resolve("contents.xcworkspacedata");
	}

	public Path getBaseDirectory() {
		return location.getParent();
	}
}
