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
package dev.nokee.xcode;

import dev.nokee.buildadapter.xcode.TestSerializableXCLoader;
import dev.nokee.xcode.workspace.XCWorkspaceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.buildadapter.xcode.TestWorkspaceReference.workspace;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.xcode.workspace.XCFileReference.group;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

@ExtendWith(MockitoExtension.class)
class WorkspaceProjectReferenceLoaderTests {
	@Mock XCLoader<XCWorkspaceData, XCWorkspaceReference> loader;
	@Mock XCProjectReferenceFactory factory;
	XCWorkspaceReference reference = workspace("Foo");
	@InjectMocks WorkspaceProjectReferencesLoader subject;

	@Nested
	class WhenEmptyWorkspace {
		@BeforeEach
		void givenEmptyWorkspace() {
			Mockito.when(loader.load(reference)).thenReturn(XCWorkspaceData.builder().build());
		}

		@Test
		void returnsNoProjectReferences() {
			assertThat(subject.load(reference), emptyIterable());
		}
	}

	@Nested
	class WhenWorkspaceContainsProjectReferences {
		@BeforeEach
		void givenWorkspaceWithProjectReferences() {
			Mockito.when(loader.load(reference)).thenReturn(XCWorkspaceData.builder().fileRef(group("Foo.xcodeproj")).fileRef(group("Pods/Bar.xcodeproj")).build());
			Mockito.when(factory.create(Paths.get("/test/Foo.xcodeproj"))).thenReturn(project("Foo"));
			Mockito.when(factory.create(Paths.get("/test/Pods/Bar.xcodeproj"))).thenReturn(project("Bar"));
		}

		@Test
		void returnsProjectReferences() {
			assertThat(subject.load(reference), contains(project("Foo"), project("Bar")));
		}
	}

	@Nested
	class WhenWorkspaceContainsNonXcodeProjectFileReference {
		@BeforeEach
		void givenWorkspaceWithNonProjectReference() {
			Mockito.when(loader.load(reference)).thenReturn(XCWorkspaceData.builder().fileRef(group("Foo.xcodeproj")).fileRef(group("README")).build());
			Mockito.when(factory.create(Paths.get("/test/Foo.xcodeproj"))).thenReturn(project("Foo"));
		}

		@Test
		void returnsOnlyProjectReferences() {
			assertThat(subject.load(reference), contains(project("Foo")));
		}
	}

	@Nested
	class WhenWorkspaceHasFileReferenceWhichContainsXcodeProjectExtension {
		@BeforeEach
		void givenWorkspaceWithNonProjectReference() {
			Mockito.when(loader.load(reference)).thenReturn(XCWorkspaceData.builder().fileRef(group("Foo.xcodeproj.bak")).build());
		}

		@Test
		void doesNotReturnNonXcodeProjectReference() {
			assertThat(subject.load(reference), emptyIterable());
		}
	}

	@Test
	void canSerialize() {
		XCLoader<XCWorkspaceData, XCWorkspaceReference> loader = new TestSerializableXCLoader<>();

		assertThat(new WorkspaceProjectReferencesLoader(loader, new DefaultXCProjectReferenceFactory()), isSerializable());
	}
}
