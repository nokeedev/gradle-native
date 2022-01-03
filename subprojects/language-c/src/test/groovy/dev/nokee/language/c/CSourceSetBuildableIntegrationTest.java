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
package dev.nokee.language.c;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;

@PluginRequirement.Require(id = "dev.nokee.c-language-base")
class CSourceSetBuildableIntegrationTest extends AbstractPluginTest {
	private CSourceSetSpec subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "mawu"))).as(CSourceSetSpec.class).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void includesSourceBuildDependenciesInSourceSetBuildDependencies() {
		val anyTask = mock(Task.class);
		val buildTask = project.getTasks().create("buildSomeSources");
		subject.getSource().from(project.getObjects().fileCollection().from("foo.txt").builtBy(buildTask));
		assertThat((Set<Task>) subject.getBuildDependencies().getDependencies(anyTask), hasItem(buildTask));
	}

	@Test
	@SuppressWarnings("unchecked")
	void includesHeadersBuildDependenciesInSourceSetBuildDependencies() {
		val anyTask = mock(Task.class);
		val buildTask = project.getTasks().create("buildSomeHeaders");
		subject.getHeaders().from(project.getObjects().fileCollection().from("foo.txt").builtBy(buildTask));
		assertThat((Set<Task>) subject.getBuildDependencies().getDependencies(anyTask), hasItem(buildTask));
	}

	@Test
	@SuppressWarnings("unchecked")
	void includesCompileTaskInBuildDependencies() {
		val anyTask = mock(Task.class);
		assertThat((Set<Task>) subject.getBuildDependencies().getDependencies(anyTask), hasItem(subject.getCompileTask().get()));
	}
}
