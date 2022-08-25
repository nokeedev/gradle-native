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
package dev.gradleplugins.testscript;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.blocks.SettingsBlock;
import dev.gradleplugins.buildscript.statements.Statement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractTestGradleBuild<SELF extends TestGradleBuild> implements TestGradleBuild {
	private final SettingsBlock.Builder settingsBuilder = SettingsBlock.builder();
	private final ProjectBlock.Builder buildBuilder = ProjectBlock.builder();
	private final Path location;
	private final Map<String, TestIncludedBuild> includedBuilds = new LinkedHashMap<>();
	private final Map<String, TestSubproject> subprojects = new LinkedHashMap<>();
	private TestBuildSrc buildSrcBuild = null;
	private String buildFileName = "build.gradle";

	protected AbstractTestGradleBuild(Path location) {
		this.location = location;
	}

	public Path getLocation() {
		return location;
	}

	public void buildFile(Consumer<? super ProjectBlock.Builder> action) {
		action.accept(buildBuilder);
		try {
			buildBuilder.build().writeTo(location.resolve(buildFileName));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public BuildScriptFile getBuildFile() {
		return new BuildScriptFile() {
			@Override
			public BuildScriptFile useKotlinDsl() {
				if (!buildFileName.endsWith(".gradle.kts")) {
					try {
						if (Files.exists(location.resolve(buildFileName))) {
							Files.delete(location.resolve(buildFileName));
						}
						buildFileName = "build.gradle.kts";
						buildBuilder.build().writeTo(location.resolve(buildFileName));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				return this;
			}

			@Override
			public BuildScriptFile useGroovyDsl() {
				if (!buildFileName.endsWith(".gradle")) {
					try {
						if (Files.exists(location.resolve(buildFileName))) {
							Files.delete(location.resolve(buildFileName));
						}
						buildFileName = "build.gradle";
						buildBuilder.build().writeTo(location.resolve(buildFileName));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
				return this;
			}

			@Override
			public BuildScriptFile append(Statement statement) {
				buildFile(it -> it.add(statement));
				return this;
			}
		};
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
	public SELF buildSrc(Consumer<? super TestBuildSrc> action) {
		if (buildSrcBuild == null) {
			buildSrcBuild = TestBuildSrc.newInstance(this);
		}
		action.accept(buildSrcBuild);

		@SuppressWarnings("unchecked")
		SELF result = (SELF) this;
		return result;
	}

	@Override
	public SELF includeBuild(String path, Consumer<? super TestIncludedBuild> action) {
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

		@SuppressWarnings("unchecked")
		SELF result = (SELF) this;
		return result;
	}

	@Override
	public SELF pluginBuild(String path, Consumer<? super TestIncludedBuild> action) {
		try {
			settingsBuilder.pluginManagement(it -> it.includeBuild(path)).build().writeTo(location.resolve("settings.gradle"));
			TestIncludedBuild includedBuild = includedBuilds.get(path);
			if (includedBuild == null) {
				includedBuild = TestIncludedBuild.newInstance(this, path);
				includedBuilds.put(path, includedBuild);
			}
			action.accept(includedBuild);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		@SuppressWarnings("unchecked")
		SELF result = (SELF) this;
		return result;
	}

	@Override
	public void file(String path, String... lines) {
		try {
			final Path location = this.location.resolve(path);
			Files.createDirectories(location.getParent());
			Files.write(location, Arrays.asList(lines));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public SELF configure(Consumer<? super TestGradleBuild> action) {
		action.accept(this);

		@SuppressWarnings("unchecked")
		SELF result = (SELF) this;
		return result;
	}
}
