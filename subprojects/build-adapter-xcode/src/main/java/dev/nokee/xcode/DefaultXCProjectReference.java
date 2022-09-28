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
package dev.nokee.xcode;

import lombok.EqualsAndHashCode;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode
public final class DefaultXCProjectReference implements XCProjectReference, Serializable {
	private /*final*/ File location;

	public DefaultXCProjectReference(Path location) {
		this.location = location.toFile();
	}

	public String getName() {
		return FilenameUtils.removeExtension(location.getName());
	}

	public Path getLocation() {
		return location.toPath();
	}

	@Override
	public String toString() {
		return "project '" + location + "'";
	}

	public XCProject load() {
		return load(XCLoaders.projectLoader());
	}
}
