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
package dev.nokee.internal.provider;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.utils.ProviderUtils.fixed;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Demonstrate automatic conversion for legacy {@literal Callable} conversion.
 */
class ProviderConvertibleFileCollectionUsageTest {
	private final Project project = rootProject();

	private static <T> ProviderConvertibleInternal<T> createSubject(Provider<T> value) {
		@SuppressWarnings("unchecked")
		val result = (ProviderConvertibleInternal<T>) Mockito.spy(ProviderConvertibleInternal.class);
		Mockito.when(result.asProvider()).thenReturn(value);
		return result;
	}

	@Test
	void unpacksToProviderWhenFileCollectionResolved() {
		val subject = createSubject(fixed(emptyList()));
		project.files(subject).getFiles();
		Mockito.verify(subject).asProvider();
	}

	@Test
	void usesProviderTaskDependenciesInFileCollection() {
		val task = project.getTasks().register("mari", Copy.class);
		val subject = createSubject(task.map(Copy::getDestinationDir));
		assertThat(project.files(subject), buildDependencies(hasItem(task.get())));
	}

	@Test
	void usesProviderValueInFileCollection() {
		val file = project.file("cido.txt");
		val subject = createSubject(fixed(file));
		assertThat(project.files(subject), hasItem(file));
	}
}
