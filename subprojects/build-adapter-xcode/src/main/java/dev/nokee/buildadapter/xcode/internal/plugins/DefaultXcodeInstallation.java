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

import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode
public class DefaultXcodeInstallation implements XcodeInstallation, Serializable {
	private /*final*/ String version;
	@EqualsAndHashCode.Exclude // same Xcode installation can be in different location
	private /*final*/ File developerDirectory;

	public DefaultXcodeInstallation(String version, Path developerDirectory) {
		assert version != null;
		assert developerDirectory != null;
		this.version = version;
		this.developerDirectory = developerDirectory.toFile();
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Path getDeveloperDirectory() {
		return developerDirectory.toPath();
	}
}
