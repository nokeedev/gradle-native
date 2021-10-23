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
package dev.nokee.model;

import com.google.common.collect.ImmutableMap;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface DependencyFactoryTester {
	DependencyFactory subject();

	@Test
	default void canCreateProjectDependencyUsingProjectNotation() {
		val dependency = subject().create(rootProject());
		assertThat(dependency, isA(ProjectDependency.class));
	}

	@Test
	default void canCreateExternalDependencyUsingMapNotation() {
		val dependency = subject().create(ImmutableMap.builder().put("group", "fim.lovemaq").put("name", "luka").put("version", "2.3").build());
		assertThat(dependency, isA(ExternalModuleDependency.class));
		assertThat(dependency, forCoordinate("fim.lovemaq", "luka", "2.3"));
	}

	@Test
	default void canCreateExternalDependencyUsingStringNotation() {
		val dependency = subject().create("jef.xipabir:dequ:5.6");
		assertThat(dependency, isA(ExternalModuleDependency.class));
		assertThat(dependency, forCoordinate("jef.xipabir", "dequ", "5.6"));
	}

	@Test
	default void throwsExceptionIfNotationIsNull() {
		assertThrows(NullPointerException.class, () -> subject().create(null));
	}
}
