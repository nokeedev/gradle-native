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
package dev.nokee.nvm.fixtures;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.blocks.SettingsBlock;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractTestGradleBuild implements TestGradleBuild {
	private final SettingsBlock.Builder settingsBuilder = SettingsBlock.builder();
	private final ProjectBlock.Builder buildBuilder = ProjectBlock.builder();
	private final Path location;
	private final Map<String, TestIncludedBuild> includedBuilds = new LinkedHashMap<>();
	private final Map<String, TestSubproject> subprojects = new LinkedHashMap<>();
	private TestBuildSrc buildSrcBuild = null;

	protected AbstractTestGradleBuild(Path location) {
		this.location = location;
	}

	public Path getLocation() {
		return location;
	}

	public void buildFile(Consumer<? super ProjectBlock.Builder> action) {
		action.accept(buildBuilder);
		try {
			buildBuilder.build().writeTo(location.resolve("build.gradle"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void settingsFile(Consumer<? super SettingsBlock.Builder> action) {
		action.accept(settingsBuilder);
		try {
			settingsBuilder.build().writeTo(location.resolve("settings.gradle"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void subproject(String path) {
		subproject(path, __ -> {});
	}

	@Override
	public void subproject(String path, Consumer<? super TestSubproject> action) {
		try {
			settingsBuilder.include(path.replace('/', ':')).build().writeTo(location.resolve("settings.gradle"));
			TestSubproject subproject = subprojects.get(path);
			if (subproject == null) {
				subproject = TestSubproject.newInstance(this, location.resolve(path));
				subprojects.put(path, subproject);
			}
			action.accept(subproject);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void buildSrc(Consumer<? super TestBuildSrc> action) {
		if (buildSrcBuild == null) {
			buildSrcBuild = TestBuildSrc.newInstance(this);
		}
		action.accept(buildSrcBuild);
	}

	@Override
	public void includeBuild(String path, Consumer<? super TestIncludedBuild> action) {
		try {
			settingsBuilder.includeBuild(path).build().writeTo(location.resolve("settings.gradle"));
			TestIncludedBuild includedBuild = includedBuilds.get(path);
			if (includedBuild == null) {
				includedBuild = TestIncludedBuild.newInstance(this, path);
				includedBuilds.put(path, includedBuild);
			}
			action.accept(includedBuild);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void file(String path, String... lines) {
		try {
			Files.write(location.resolve(path), Arrays.asList(lines));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
