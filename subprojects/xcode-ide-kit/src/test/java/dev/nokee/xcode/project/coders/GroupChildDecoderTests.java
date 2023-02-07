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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.project.CodeablePBXFileReference;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupChildDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);
	GroupChildDecoder<?> subject = new GroupChildDecoder<>();

	@Nested
	class WhenIsaPBXFileReference {
		KeyedObject map = new IsaKeyedObject("PBXFileReference");

		@Test
		void createsPBXFileReference() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXFileReference.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXGroup {
		KeyedObject map = new IsaKeyedObject("PBXGroup");

		@Test
		void createsPBXGroup() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXGroup.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXReferenceProxy {
		KeyedObject map = new IsaKeyedObject("PBXReferenceProxy");

		@Test
		void createsPBXReferenceProxy() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXReferenceProxy.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXVariantGroup {
		KeyedObject map = new IsaKeyedObject("PBXVariantGroup");

		@Test
		void createsPBXVariantGroup() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXVariantGroup.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaXCVersionGroup {
		KeyedObject map = new IsaKeyedObject("XCVersionGroup");

		@Test
		void createsXCVersionGroup() {
			assertThat(subject.decode(map, context), equalTo(CodeableXCVersionGroup.newInstance(map)));
		}
	}

	@Test
	void throwsExceptionOnUnexpectedIsaValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(new IsaKeyedObject("PBXUnknown"), context));
	}
}
