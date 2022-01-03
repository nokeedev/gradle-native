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
package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasCompileTask;
import lombok.val;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;

public interface LanguageSourceSetHasBuildableCompileTaskIntegrationTester<T extends LanguageSourceSet & Buildable & HasCompileTask> {
	T subject();

	@Test
	@SuppressWarnings("unchecked")
	default void includesCompileTaskInBuildDependencies() {
		val anyTask = mock(Task.class);
		assertThat((Set<Task>) subject().getBuildDependencies().getDependencies(anyTask), hasItem(subject().getCompileTask().get()));
	}
}
