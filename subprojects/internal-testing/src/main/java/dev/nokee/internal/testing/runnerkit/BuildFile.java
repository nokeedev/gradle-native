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

import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.internal.testing.runnerkit.GradleDsl.GROOVY;
import static dev.nokee.internal.testing.runnerkit.GradleDsl.KOTLIN;

public final class BuildFile {
	private final String id = "build";
	private final List<Section> sections;

	public BuildFile(List<Section> sections) {
		this.sections = sections;
	}

	public static final class Builder {
		private final List<Section> sections = new ArrayList<>();
		private final Set<String> alreadyCreatedConfigurations = new HashSet<>();

		public Builder apply(ApplyNotation notation) {
			sections.add(ApplySection.apply(notation));
			return this;
		}

		public Builder repositories(Consumer<? super RepositoriesSectionBuilder> builderAction) {
			val builder = new RepositoriesSectionBuilder();
			builderAction.accept(builder);
			sections.add(builder.build());
			return this;
		}

		public Builder dependencies(Consumer<? super DependenciesSectionBuilder> builderAction) {
			val builder = new DependenciesSectionBuilder();
			builderAction.accept(builder);
			sections.add(builder.build());
			return this;
		}

		public Builder configurations(Consumer<? super ConfigurationsSectionBuilder> builderAction) {
			val builder = new ConfigurationsSectionBuilder(alreadyCreatedConfigurations);
			builderAction.accept(builder);
			sections.add(builder.build());
			return this;
		}

		public BuildFile build() {
			return new BuildFile(sections);
		}
	}

	public void generate(GradleDsl dsl, Path targetDirectory) throws IOException {
		switch (dsl) {
			case GROOVY:
				new GroovyWriter().writeTo(targetDirectory.resolve(id + ".gradle"));
				break;
			case KOTLIN:
				new KotlinWriter().writeTo(targetDirectory.resolve(id + ".gradle.kts"));
				break;
		}
	}

	private final class GroovyWriter {
		void writeTo(Path buildFile) throws IOException {
			Files.write(buildFile, sections.stream().map(it -> it.generateSection(GROOVY)).collect(Collectors.toList()));
		}
	}

	private final class KotlinWriter {
		void writeTo(Path buildFile) throws IOException {
			Files.write(buildFile, sections.stream().map(it -> it.generateSection(KOTLIN)).collect(Collectors.toList()));
		}
	}
}
