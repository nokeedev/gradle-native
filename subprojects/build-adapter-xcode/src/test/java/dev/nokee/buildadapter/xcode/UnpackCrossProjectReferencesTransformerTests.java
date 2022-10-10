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

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import dev.nokee.buildadapter.xcode.internal.plugins.UnpackCrossProjectReferencesTransformer;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnpackCrossProjectReferencesTransformerTests {
	@Nested
	class WhenInputProjectTransformed {
		@Mock XCLoader<Iterable<XCProjectReference>, XCProjectReference> loader;
		@InjectMocks UnpackCrossProjectReferencesTransformer subject;
		Set<XCProjectReference> inputProjects;

		@Nested
		class WhenNoProject {
			@BeforeEach
			void givenNoProject() {
				inputProjects = Collections.emptySet();
			}

			@Test
			void returnsEmptyProjectList() {
				assertThat(subject.transform(inputProjects), emptyIterable());
			}
		}

		@Nested
		class WhenProjectsTransformed {
			@BeforeEach
			void givenNoCrossProjectReferences() {
				inputProjects = ImmutableSet.of(project("B"), project("A"));

				// No cross project references
				when(loader.load(any())).thenReturn(emptyList());
			}

			@Test
			void returnsInputProjectReferences() {
				assertThat(subject.transform(inputProjects), containsInAnyOrder(project("B"), project("A")));
			}
		}

		@Nested
		class WhenLProjectsTransformedContainingCrossProjectReferences {
			@BeforeEach
			void givenCrossProjectReferences() {
				inputProjects = ImmutableSet.of(project("B"), project("A"));

				// Locate cross-project on project A
				when(loader.load(any())).thenReturn(emptyList());
				when(loader.load(project("A")))
					.thenReturn(asList(project("A2"), project("A1")));
			}

			@Test
			void includesCrossProjectReferences() {
				assertThat(subject.transform(inputProjects), hasItems(project("A2"), project("A1")));
			}

			@Test
			void includesLocatedProjectReferences() {
				assertThat(subject.transform(inputProjects), hasItems(project("B"), project("A")));
			}
		}

		@Nested
		class WhenCrossProjectReferencesContainsMoreCrossProjectReferences {
			@BeforeEach
			void givenCrossProjectReferences() {
				inputProjects = ImmutableSet.of(project("A"));

				// Locate cross-project on projects
				when(loader.load(any())).thenReturn(emptyList());
				when(loader.load(project("A"))).thenReturn(asList(project("B"), project("C")));
				when(loader.load(project("C"))).thenReturn(singletonList(project("D")));
				when(loader.load(project("D"))).thenReturn(singletonList(project("E")));
			}

			@Test
			void includesNestedCrossProjectReferences() {
				assertThat(subject.transform(inputProjects),
					hasItems(project("B"), project("C"), project("D"), project("E")));
			}
		}

		@Nested
		class WhenCrossProjectReferencesIncludesInputProjects {
			@BeforeEach
			void givenNoCrossProjectReferences() {
				inputProjects = ImmutableSet.of(project("A"));

				// Load duplicated project references
				when(loader.load(any())).thenReturn(emptyList());
				when(loader.load(project("A"))).thenReturn(asList(project("B"), project("C")));
				when(loader.load(project("B"))).thenReturn(asList(project("C") /*duplicated*/, project("E")));
				when(loader.load(project("C"))).thenReturn(asList(project("D"), project("E") /*duplicated*/));
			}

			@Test
			void ignoresDuplicatedProjectReferences() {
				assertThat(subject.transform(inputProjects),
					containsInAnyOrder(project("A"), project("B"), project("C"),
						project("E"), project("D")));
			}
		}
	}

	@Test
	void canSerialize() {
		XCLoader<Iterable<XCProjectReference>, XCProjectReference> loader = new TestSerializableXCLoader<>();

		assertThat(new UnpackCrossProjectReferencesTransformer(loader), isSerializable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquality() {
		XCLoader<Iterable<XCProjectReference>, XCProjectReference> loaderA = __ -> { throw new UnsupportedOperationException(); };
		XCLoader<Iterable<XCProjectReference>, XCProjectReference> loaderB = __ -> { throw new UnsupportedOperationException(); };

		new EqualsTester()
			.addEqualityGroup(
				new UnpackCrossProjectReferencesTransformer(loaderA),
				new UnpackCrossProjectReferencesTransformer(loaderA))
			.addEqualityGroup(new UnpackCrossProjectReferencesTransformer(loaderB))
			.testEquals();
	}
}
