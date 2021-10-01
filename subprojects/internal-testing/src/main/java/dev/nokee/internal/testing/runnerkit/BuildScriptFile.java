/*
 * Copyright 2021 the original author or authors.
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

package dev.nokee.internal.testing.runnerkit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class BuildScriptFile {
	private final Path path;
	private final GradleDsl dsl;
	private final List<Section> sections = new ArrayList<>();

	static BuildScriptFile createFile(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		Files.createFile(path);
		return new BuildScriptFile(path, guessGradleDslFromPath(path));
	}

	private static GradleDsl guessGradleDslFromPath(Path path) {
		if (path.getFileName().toString().endsWith("." + GradleDsl.GROOVY.getFileExtension())) {
			return GradleDsl.GROOVY;
		} else {
			return GradleDsl.KOTLIN;
		}
	}

	public BuildScriptFile(Path path) {
		this(path, guessGradleDslFromPath(path));
	}

	private BuildScriptFile(Path path, GradleDsl dsl) {
		this.path = path;
		this.dsl = dsl;
	}

	public BuildScriptFile leftShift(Object text) throws IOException {
		return append(text);
	}

	public BuildScriptFile leftShift(Section section) throws IOException {
		return append(section);
	}

	public BuildScriptFile append(Section section) throws IOException {
		sections.add(section);
		Files.write(path, (section.generateSection(dsl) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		return this;
	}

	public BuildScriptFile append(Object text) throws IOException {
		sections.add(dsl.newSection(text));
		Files.write(path, (text.toString() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		return this;
	}

	public String getText() throws IOException {
		return new String(Files.readAllBytes(path));
	}

	public File toFile() {
		return path.toFile();
	}

	public Path toPath() {
		return path;
	}
}
