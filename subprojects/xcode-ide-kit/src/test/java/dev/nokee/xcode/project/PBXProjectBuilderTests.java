/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode.project;

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

class PBXProjectBuilderTests {
	@Nested
	class WhenParent {
		KeyedObject parent = DefaultKeyedObject.builder().put(CodeablePBXProject.CodingKeys.mainGroup, newAlwaysThrowingMock(PBXGroup.class)).build();
		PBXProject.Builder subject = new PBXProject.Builder(parent);

		@Test
		void canReplaceMainGroup() {
			PBXGroup newMainGroup = newAlwaysThrowingMock(PBXGroup.class);

			assertThat(subject.mainGroup(newMainGroup).build().getMainGroup(), equalTo(newMainGroup));
		}

		@Test
		void canReplaceMainGroupViaChildrenBuilders() {
			val newMainGroup = subject.group(it -> it.path("Foo").child(PBXFileReference.ofGroup("Foo.h"))) //
				.file(PBXFileReference.ofSourceRoot("makefile")).build().getMainGroup();
			assertThat(newMainGroup.getChildren().get(0).getPath(), optionalWithValue(equalTo("Foo")));
			assertThat(newMainGroup.getChildren().get(1), equalTo(PBXFileReference.ofSourceRoot("makefile")));
		}
	}

	@Nested
	class WhenNoMainGroupSpecified {
		PBXProject subject = new PBXProject.Builder().build();

		@Test
		void createsEmptyMainGroup() {
			assertThat(subject.getMainGroup().getName(), optionalWithValue(equalTo("mainGroup")));
			assertThat(subject.getMainGroup().getChildren(), emptyIterable());
			assertThat(subject.getMainGroup().getSourceTree(), equalTo(PBXSourceTree.GROUP));
		}
	}
}
