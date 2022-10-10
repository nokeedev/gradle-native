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

import com.google.common.testing.EqualsTester;
import dev.nokee.buildadapter.xcode.internal.plugins.LoadWorkspaceProjectReferencesIfAvailableTransformer;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspaceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.buildadapter.xcode.TestWorkspaceReference.workspace;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadWorkspaceProjectReferencesIfAvailableTransformerTests {
	@Nested
	class WhenInputWorkspaceTransformed {
		@Mock Supplier<Set<XCProjectReference>> projectsSupplier;
		@Mock XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> loader;
		@InjectMocks LoadWorkspaceProjectReferencesIfAvailableTransformer subject;

		@Nested
		class WhenWorkspaceAbsent {
			@BeforeEach
			void givenNoWorkspace() {
				when(projectsSupplier.get()).thenReturn(of(project("A"), project("B")));
			}

			@Test
			void returnsDefaultSuppliedProjects() {
				assertThat(subject.transform(null), contains(project("A"), project("B")));
			}
		}

		@Nested
		class WhenWorkspacePresent {
			@BeforeEach
			void givenWorkspace() {
				when(loader.load(workspace("A"))).thenReturn(asList(project("A1"), project("A2")));
			}

			@Test
			void returnsProjectsFromWorkspace() {
				assertThat(subject.transform(workspace("A")),
					containsInAnyOrder(project("A1"), project("A2")));
			}
		}
	}

	@Test
	void canSerialize() {
		XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> loader = new TestSerializableXCLoader<>();
		Supplier<Set<XCProjectReference>> projectsSupplier = ofInstance(of(project("A"), project("B")));

		assertThat(new LoadWorkspaceProjectReferencesIfAvailableTransformer(projectsSupplier, loader), isSerializable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquality() {
		XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> loaderA = __ -> { throw new UnsupportedOperationException(); };
		XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> loaderB = __ -> { throw new UnsupportedOperationException(); };
		Supplier<Set<XCProjectReference>> supplierA = () -> { throw new UnsupportedOperationException(); };
		Supplier<Set<XCProjectReference>> supplierB = () -> { throw new UnsupportedOperationException(); };

		new EqualsTester()
			.addEqualityGroup(
				new LoadWorkspaceProjectReferencesIfAvailableTransformer(supplierA, loaderA),
				new LoadWorkspaceProjectReferencesIfAvailableTransformer(supplierA, loaderA))
			.addEqualityGroup(new LoadWorkspaceProjectReferencesIfAvailableTransformer(supplierA, loaderB))
			.addEqualityGroup(new LoadWorkspaceProjectReferencesIfAvailableTransformer(supplierB, loaderA))
			.testEquals();
	}
}
