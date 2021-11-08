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
package dev.nokee.platform.jni;

import dev.nokee.platform.base.testers.ArtifactTester;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.tasks.bundling.Jar;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

public interface JarBinaryTester<T extends JarBinary> extends ArtifactTester<T> {
	T subject();

	@Test
	default void hasJarTask() {
		assertThat(subject().getJarTask(), providerOf(isA(Jar.class)));
	}

	@Test
	default void includesJarTaskAsBuildable() {
		val anyTask = mock(Task.class);
		val subject = subject();
		assertThat((Set<Task>) subject.getBuildDependencies().getDependencies(anyTask),
			hasItem(subject.getJarTask().get()));
	}
}
