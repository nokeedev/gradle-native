/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XcodeDeveloperDirectoryEnvironmentVariableLocator implements XcodeDeveloperDirectoryLocator {
	private final FileSystem fileSystem;
	private final DeveloperDirEnvironmentVariable developerDir;
	private final XcodeDeveloperDirectoryLocator delegate;

	public XcodeDeveloperDirectoryEnvironmentVariableLocator(FileSystem fileSystem, DeveloperDirEnvironmentVariable developerDir, XcodeDeveloperDirectoryLocator delegate) {
		this.fileSystem = fileSystem;
		this.developerDir = developerDir;
		this.delegate = delegate;
	}

	@Override
	public Path locate() {
		final String value = developerDir.get();
		if (value == null) {
			return delegate.locate();
		}

		// Assume DEVELOPER_DIR points to `Xcode.app/Content/Developer`
		Path developerDirPath = fileSystem.getPath(value);
		if (developerDirPath.endsWith("Content/Developer") && Files.isDirectory(developerDirPath)) {
			return developerDirPath;
		}

		// Assume DEVELOPER_DIR points to `Xcode.app`, check for the `Content/Developer` path
		developerDirPath = developerDirPath.resolve("Content/Developer");
		if (Files.isDirectory(developerDirPath)) {
			return developerDirPath;
		}

		return delegate.locate();
	}
}
