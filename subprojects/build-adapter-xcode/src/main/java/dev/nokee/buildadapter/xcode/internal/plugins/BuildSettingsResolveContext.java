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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCFileReference;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Objects;

public final class BuildSettingsResolveContext implements XCFileReference.ResolveContext {
	private final FileSystem fileSystem;
	private final XCBuildSettings buildSettings;

	public BuildSettingsResolveContext(FileSystem fileSystem, XCBuildSettings buildSettings) {
		this.fileSystem = fileSystem;
		this.buildSettings = buildSettings;
	}

	@Override
	public Path getBuiltProductsDirectory() {
		return get("BUILT_PRODUCTS_DIR");
	}

	@Override
	public Path get(String name) {
		return fileSystem.getPath(Objects.requireNonNull(buildSettings.get(name), () -> "could not resolve build setting '" + name + "'"));
	}
}
