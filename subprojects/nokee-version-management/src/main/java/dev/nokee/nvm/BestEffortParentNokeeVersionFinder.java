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
package dev.nokee.nvm;

import org.gradle.api.invocation.Gradle;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class BestEffortParentNokeeVersionFinder implements Callable<NokeeVersion> {
	private final DefaultNokeeVersionLoader versionLoader = DefaultNokeeVersionLoader.INSTANCE;
	private final Gradle gradle;

	public BestEffortParentNokeeVersionFinder(Gradle gradle) {
		this.gradle = gradle;
	}

	@Override
	@Nullable
	public NokeeVersion call() throws Exception {
		if (gradle.getParent() == null) {
			return null;
		}
		final Path currentDirectory = gradle.getParent().getStartParameter().getCurrentDir().toPath();
		Path settingsFile = currentDirectory.resolve("settings.gradle");
		final Pattern pluginPattern = Pattern.compile("id[\\( ][\"']dev\\.nokee\\.nokee-version-management[\"']\\)?");
		if (Files.notExists(settingsFile)) {
			settingsFile = currentDirectory.resolve("settings.gradle.kts");
		}
		try (final Stream<String> lineStream = Files.lines(settingsFile)) {
			if (lineStream.anyMatch(it -> pluginPattern.matcher(it).find())) {
				final Path versionFile = currentDirectory.resolve(".nokee-version");
				return versionLoader.fromFile(versionFile);
			}
		}
		return null;
	}
}
