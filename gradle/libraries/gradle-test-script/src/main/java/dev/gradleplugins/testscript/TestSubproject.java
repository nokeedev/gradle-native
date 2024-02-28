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
import dev.gradleplugins.buildscript.io.GradleBuildFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class TestSubproject implements HasBuildFile {
	private final GradleBuildFile buildFile;
	private final TestGradleBuild parent;

	private TestSubproject(TestGradleBuild parent, Path projectDirectory) {
		this.parent = parent;
		this.buildFile = GradleBuildFile.inDirectory(projectDirectory);
	}

	static TestSubproject newInstance(TestGradleBuild parent, Path projectDirectory) {
		try {
			Files.createDirectories(projectDirectory);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return new TestSubproject(parent, projectDirectory);
	}

	public void parentBuild(Consumer<? super TestGradleBuild> action) {
		action.accept(parent);
	}

	@Override
	public void buildFile(Consumer<? super ProjectBlock> action) {
		buildFile.configure(action);
	}

	@Override
	public GradleBuildFile getBuildFile() {
		return buildFile;
	}
}
