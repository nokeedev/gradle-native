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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.AllXCProjectReferencesValueSource;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllXCProjectReferencesValueSourceTests {
	@Mock
	XCLoader<Iterable<XCProjectReference>, XCProjectReference> loader;
	AllXCProjectReferencesValueSource.Parameters parameters;
	AllXCProjectReferencesValueSource subject;

	@BeforeEach
	void createSubject() {
		parameters = objectFactory().newInstance(AllXCProjectReferencesValueSource.Parameters.class);
		subject = new AllXCProjectReferencesValueSource(loader) {
			@Override
			public Parameters getParameters() {
				return parameters;
			}
		};
	}

	@Test
	void returnsEmptyIterableWhenNoInputProjectReferences() {
		parameters.getProjectLocations().empty();

		assertThat(subject.obtain(), emptyIterable());
	}

	@Test
	void returnsInputProjectReferencesWhenNoneHaveCrossProjectReferences() {
		parameters.getProjectLocations().set(asList(project("B"), project("A")));
		when(loader.load(any())).thenReturn(Collections.emptyList());

		assertThat(subject.obtain(), contains(project("B"), project("A")));
	}

	@Test
	void returnsCrossProjectReferencesAlongSideWithInputProjectReferences() {
		parameters.getProjectLocations().set(asList(project("B"), project("A")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A")))
			.thenReturn(asList(project("A2"), project("A1")));

		assertThat(subject.obtain(), contains(project("B"), project("A"),
			project("A2"), project("A1")));
	}

	@Test
	void loadsCrossProjectReferencesOnAllInputProjectReferencesAndDiscoveredProjectReferences() {
		parameters.getProjectLocations().set(singletonList(project("A")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A"))).thenReturn(asList(project("B"), project("C")));
		when(loader.load(project("C"))).thenReturn(singletonList(project("D")));
		when(loader.load(project("D"))).thenReturn(singletonList(project("E")));

		subject.obtain();

		val inOrder = Mockito.inOrder(loader);
		inOrder.verify(loader).load(project("A"));
		inOrder.verify(loader).load(project("B"));
		inOrder.verify(loader).load(project("C"));
		inOrder.verify(loader).load(project("D"));
		inOrder.verify(loader).load(project("E"));
	}

	@Test
	void ignoresDuplicatedProjectReferences() {
		parameters.getProjectLocations().set(singletonList(project("A")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A"))).thenReturn(asList(project("B"), project("C")));
		when(loader.load(project("B"))).thenReturn(asList(project("C"), project("E")));
		when(loader.load(project("C"))).thenReturn(asList(project("D"), project("E")));

		assertThat(subject.obtain(), contains(project("A"), project("B"), project("C"), project("E"), project("D")));
	}
}
