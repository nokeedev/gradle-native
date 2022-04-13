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
			// DirectoryStream returns element in no particular order.
			// For deterministic reason, we will sort the paths according to their natural ordering.
			// It's important to note the ordering should not be considered natural, only deterministic.
			return ImmutableList.sortedCopyOf(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to locate Xcode project.", e);
		}
	}

	private boolean filterXcodeProject(Path entry) {
		return entry.getFileName().toString().endsWith(".xcodeproj");
	}
}
