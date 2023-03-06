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

public final class XcodeDeveloperDirectorySystemApplicationsLocator implements XcodeDeveloperDirectoryLocator {
	private final FileSystem fileSystem;
	private final XcodeDeveloperDirectoryLocator delegate;

	public XcodeDeveloperDirectorySystemApplicationsLocator(FileSystem fileSystem, XcodeDeveloperDirectoryLocator delegate) {
		this.fileSystem = fileSystem;
		this.delegate = delegate;
	}

	@Override
	public Path locate() {
		final Path developerDirPath = fileSystem.getPath("/Applications/Xcode.app/Contents/Developer");
		if (Files.isDirectory(developerDirPath)) {
			return developerDirPath;
		} else {
			return delegate.locate();
		}
	}
}
