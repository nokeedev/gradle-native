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
package dev.nokee.buildadapter.xcode.dev.nokee.buildadapter.xcode;

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

import static dev.nokee.buildadapter.xcode.dev.nokee.buildadapter.xcode.TestProjectReference.project;
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
		parameters.getProjectLocations().set(asList(project("B.xcodeproj"), project("A.xcodeproj")));
		when(loader.load(any())).thenReturn(Collections.emptyList());

		assertThat(subject.obtain(), contains(project("B.xcodeproj"), project("A.xcodeproj")));
	}

	@Test
	void returnsCrossProjectReferencesAlongSideWithInputProjectReferences() {
		parameters.getProjectLocations().set(asList(project("B.xcodeproj"), project("A.xcodeproj")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A.xcodeproj")))
			.thenReturn(asList(project("A2.xcodeproj"), project("A1.xcodeproj")));

		assertThat(subject.obtain(), contains(project("B.xcodeproj"), project("A.xcodeproj"),
			project("A2.xcodeproj"), project("A1.xcodeproj")));
	}

	@Test
	void loadsCrossProjectReferencesOnAllInputProjectReferencesAndDiscoveredProjectReferences() {
		parameters.getProjectLocations().set(singletonList(project("A.xcodeproj")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A.xcodeproj"))).thenReturn(asList(project("B.xcodeproj"), project("C.xcodeproj")));
		when(loader.load(project("C.xcodeproj"))).thenReturn(singletonList(project("D.xcodeproj")));
		when(loader.load(project("D.xcodeproj"))).thenReturn(singletonList(project("E.xcodeproj")));

		subject.obtain();

		val inOrder = Mockito.inOrder(loader);
		inOrder.verify(loader).load(project("A.xcodeproj"));
		inOrder.verify(loader).load(project("B.xcodeproj"));
		inOrder.verify(loader).load(project("C.xcodeproj"));
		inOrder.verify(loader).load(project("D.xcodeproj"));
		inOrder.verify(loader).load(project("E.xcodeproj"));
	}

	@Test
	void ignoresDuplicatedProjectReferences() {
		parameters.getProjectLocations().set(singletonList(project("A.xcodeproj")));
		when(loader.load(any())).thenReturn(Collections.emptyList());
		when(loader.load(project("A.xcodeproj"))).thenReturn(asList(project("B.xcodeproj"), project("C.xcodeproj")));
		when(loader.load(project("B.xcodeproj"))).thenReturn(asList(project("C.xcodeproj"), project("E.xcodeproj")));
		when(loader.load(project("C.xcodeproj"))).thenReturn(asList(project("D.xcodeproj"), project("E.xcodeproj")));

		assertThat(subject.obtain(), contains(project("A.xcodeproj"), project("B.xcodeproj"), project("C.xcodeproj"), project("E.xcodeproj"), project("D.xcodeproj")));
	}
}
