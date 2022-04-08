/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.xcode;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class XCWorkspaceElement extends SourceElement {
	private final String workspaceName;
	private final ImmutableList<String> paths;

	public XCWorkspaceElement(String workspaceName, String... paths) {
		this.workspaceName = workspaceName;
		this.paths = ImmutableList.copyOf(paths);
	}

	@Override
	public List<SourceFile> getFiles() {
		return Arrays.asList(
			sourceFile(workspaceName + ".xcworkspace", "contents.xcworkspacedata", Stream.of(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<Workspace version = \"1.0\">",
				paths.stream().map(it -> "\t<FileRef location=\"group:" + it + ".xcodeproj\"/>").collect(joining("\n")),
				"</Workspace>").collect(joining(System.lineSeparator()))),
			sourceFile(workspaceName + ".xcworkspace/xcshareddata", "IDEWorkspaceChecks.plist", Stream.of(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">",
				"<plist version=\"1.0\">",
				"<dict>",
				"\t<key>IDEDidComputeMac32BitWarning</key>",
				"\t<true/>",
				"</dict>",
				"</plist>").collect(joining(System.lineSeparator())))
		);
	}

	@Override
	public void writeToProject(File projectDir) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(projectDir);
		}
	}

	public void writeToProject(Path projectDirectory) {
		writeToProject(projectDirectory.toFile());
	}
}
