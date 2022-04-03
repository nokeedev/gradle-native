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
package dev.nokee.xcode.workspace;

import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.workspace.XCFileReference.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XCWorkspaceDataTest {
	private final XCWorkspaceData subject = XCWorkspaceData.builder().fileRef(of("group:a.xcodeproj")).fileRef(of("absolute:/b/c/d.xcodeproj")).build();

	@Test
	void hasFileReferences() {
		assertThat(subject.getFileRefs(), containsInAnyOrder(of("absolute:/b/c/d.xcodeproj"), of("group:a.xcodeproj")));
	}

	@Test
	void hasImmutableFileReferences() {
		assertThrows(RuntimeException.class, () -> subject.getFileRefs().add(of("group:b.xcodeproj")));
	}
}
