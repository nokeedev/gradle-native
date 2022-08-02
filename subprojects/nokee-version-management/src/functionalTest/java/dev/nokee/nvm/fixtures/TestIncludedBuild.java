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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class TestIncludedBuild extends AbstractTestGradleBuild<TestIncludedBuild> implements TestGradleBuild, Configurable<TestIncludedBuild> {
	private final TestGradleBuild parent;
	private final String buildPath;

	private TestIncludedBuild(TestGradleBuild parent, Path location, String buildPath) {
		super(location);
		this.parent = parent;
		this.buildPath = buildPath;
	}

	static TestIncludedBuild newInstance(TestGradleBuild parent, String buildPath) {
		try {
			final Path location = parent.getLocation().resolve(buildPath);
			Files.createDirectories(location); // ensure all directories exists

			// Note: included builds require a settings.gradle[.kts] even if empty
			Files.createFile(location.resolve("settings.gradle"));
			return new TestIncludedBuild(parent, location, buildPath);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String buildPath() {
		return buildPath;
	}

	public void parentBuild(Consumer<? super TestGradleBuild> action) {
		action.accept(parent);
	}

	@Override
	public TestIncludedBuild configure(Consumer<? super TestIncludedBuild> action) {
		action.accept(this);
		return this;
	}
}
