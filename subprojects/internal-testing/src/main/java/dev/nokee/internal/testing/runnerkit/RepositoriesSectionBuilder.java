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

import java.util.ArrayList;
import java.util.List;

public final class RepositoriesSectionBuilder {
	private final List<Section> sections = new ArrayList<>();

	public RepositoriesSectionBuilder gradlePluginPortal() {
		sections.add(new Section() {
			@Override
			public String generateSection(GradleDsl dsl) {
				return "gradlePluginPortal()";
			}
		});
		return this;
	}

	public RepositoriesSectionBuilder mavenCentral() {
		sections.add(new Section() {
			@Override
			public String generateSection(GradleDsl dsl) {
				return "mavenCentral()";
			}
		});
		return this;
	}

	Section build() {
		return new NamedSection("repositories", sections);
	}
}
