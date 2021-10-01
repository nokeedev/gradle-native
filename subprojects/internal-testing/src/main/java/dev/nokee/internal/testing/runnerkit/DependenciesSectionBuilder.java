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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DependenciesSectionBuilder {
	private final List<Section> sections = new ArrayList<>();

	public DependenciesSectionBuilder add(String name, String notation) {
		sections.add(new DependencySpec(name, DependencyNotation.fromString(notation)));
		return this;
	}

	public DependenciesSectionBuilder add(String name, DependencyNotation notation) {
		sections.add(new DependencySpec(name, notation));
		return this;
	}

	public static Consumer<? super DependenciesSectionBuilder> classpath(DependencyNotation notation) {
		return dependencies -> dependencies.add("classpath", notation);
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
			val string = notation.toString(dsl);
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
