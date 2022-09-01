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
package dev.nokee.utils;

import dev.nokee.internal.testing.GradleProviderMatchers;
import dev.nokee.internal.testing.ProjectMatchers;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiFunction;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.utils.ProviderUtils.resolve;
import static dev.nokee.utils.ProviderUtils.zip;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_ZipTest {
	@Mock BiFunction<String, String, String> combiner;

	@Test
	void returnsResultOfCombinerWhenZippedProviderRealized() {
		val subject = zip(() -> objectFactory().listProperty(Object.class), providerFactory().provider(() -> "first"), providerFactory().provider(() -> "second"), combiner);
		Mockito.when(combiner.apply(any(), any())).thenReturn("third");
		assertThat(subject, providerOf("third"));
	}

	@Test
	void callsCombinerWithBothProviderValuesWhenZippedProviderRealized() {
		val subject = zip(() -> objectFactory().listProperty(Object.class), providerFactory().provider(() -> "first"), providerFactory().provider(() -> "second"), combiner);
		resolve(subject);
		verify(combiner).apply("first", "second");
	}

	@Test
	void doesNotCallCombinerWhenZippedProviderNotRealized() {
		zip(() -> objectFactory().listProperty(Object.class), providerFactory().provider(() -> "first"), providerFactory().provider(() -> "second"), combiner);
		verify(combiner, never()).apply("first", "second");
	}

	@Test
	void mergesImplicitTaskDependenciesFromBothProvidersInZippedProvider() {
		val project = rootProject();
		val taskDependencies = project.files(zip(() -> objectFactory().listProperty(Object.class), project.getTasks().register("firstTask").map(__ -> "first"), project.getTasks().register("secondTask").map(__ -> "second"), combiner));
		assertThat(taskDependencies, ProjectMatchers.buildDependencies(containsInAnyOrder(named("firstTask"), named("secondTask"))));
	}
}
