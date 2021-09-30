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

import com.google.common.collect.Streams;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class DependenciesSectionBuilder {
	private final List<Section> sections = new ArrayList<>();

	public DependenciesSectionBuilder add(String name, String notation) {
		sections.add(new DependencySpec(name, new StringLiteralDependencyNotation(notation)));
		return this;
	}

	public DependenciesSectionBuilder add(String name, DependencyNotation notation) {
		sections.add(new DependencySpec(name, notation));
		return this;
	}

	public static Consumer<? super DependenciesSectionBuilder> classpath(DependencyNotation notation) {
		return dependencies -> dependencies.add("classpath", notation);
	}

	public interface DependencyNotation {
		static DependencyNotation files(Iterable<? extends File> files) {
			return new FileCollectionDependencyNotation(files);
		}
	}

	private static final class StringLiteralDependencyNotation extends AbstractSection implements DependencyNotation {
		private final String s;

		private StringLiteralDependencyNotation(String s) {
			this.s = s;
		}

		@Override
		protected String getGroovy() {
			return quote(s);
		}

		@Override
		protected String getKotlin() {
			return quote(s);
		}

		private String quote(String s) {
			return "\"" + s + "\"";
		}
	}

	private static final class FileCollectionDependencyNotation extends AbstractSection implements DependencyNotation {
		private final Iterable<? extends File> files;

		public FileCollectionDependencyNotation(Iterable<? extends File> files) {
			this.files = files;
		}

		@Override
		protected String getGroovy() {
			return "files(" + Streams.stream(files).map(File::toURI).map(Objects::toString).map(this::quote).collect(Collectors.joining(", ")) + ")";
		}

		@Override
		protected String getKotlin() {
			return "files(" + Streams.stream(files).map(File::toURI).map(Objects::toString).map(this::quote).collect(Collectors.joining(", ")) + ")";
		}

		private String quote(String s) {
			return "\"" + s + "\"";
		}
	}

	Section build() {
		return new NamedSection("dependencies", sections);
	}

	static class DependencySpec extends AbstractSection {
		private final String targetConfiguration;
		private final DependencyNotation notation;

		DependencySpec(String configuration, DependencyNotation notation) {
			this.targetConfiguration = configuration;
			this.notation = notation;
		}

		private String formatNotation(GradleDsl dsl) {
			val string = ((Section) notation).generateSection(dsl);
			switch (dsl) {
				case GROOVY:
					return string;
				case KOTLIN:
					return "(" + string + ")";
				default:
					throw new IllegalStateException("Unexpected value: " + dsl);
			}
		}

		@Override
		protected String getGroovy() {
			return targetConfiguration + " " + formatNotation(GradleDsl.GROOVY);
		}

		@Override
		protected String getKotlin() {
			return targetConfiguration + formatNotation(GradleDsl.KOTLIN);
		}
	}
}
