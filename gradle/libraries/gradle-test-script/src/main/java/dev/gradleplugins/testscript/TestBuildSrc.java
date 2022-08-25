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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class TestBuildSrc extends AbstractTestGradleBuild<TestBuildSrc> implements TestGradleBuild {
	private final TestGradleBuild parent;

	public TestBuildSrc(TestGradleBuild parent, Path location) {
		super(location);
		this.parent = parent;
	}

	static TestBuildSrc newInstance(TestGradleBuild parent) {
		try {
			final Path location = parent.getLocation().resolve("buildSrc");
			Files.createDirectories(location);
			// Note: buildSrc doesn't require a settings.gradle[.kts]
			return new TestBuildSrc(parent, location);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void parentBuild(Consumer<? super TestGradleBuild> action) {
		action.accept(parent);
	}
}
