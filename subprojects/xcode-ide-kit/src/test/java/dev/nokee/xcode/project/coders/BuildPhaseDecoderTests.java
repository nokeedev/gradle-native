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

import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.CodeablePBXSourcesBuildPhase;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildPhaseDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);
	BuildPhaseDecoder<?> subject = new BuildPhaseDecoder<>();

	@Nested
	class WhenIsaPBXCopyFilesBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXCopyFilesBuildPhase");

		@Test
		void createsPBXCopyFilesBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXCopyFilesBuildPhase.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXFrameworksBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXFrameworksBuildPhase");

		@Test
		void createsPBXFrameworksBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXFrameworksBuildPhase.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXHeadersBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXHeadersBuildPhase");

		@Test
		void createsPBXHeadersBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXHeadersBuildPhase.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXResourcesBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXResourcesBuildPhase");

		@Test
		void createsPBXResourcesBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXResourcesBuildPhase.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXShellScriptBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXShellScriptBuildPhase");

		@Test
		void createsPBXShellScriptBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXShellScriptBuildPhase.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXSourcesBuildPhase {
		KeyedObject map = new IsaKeyedObject("PBXSourcesBuildPhase");

		@Test
		void createsPBXSourcesBuildPhase() {
			assertThat(subject.decode(map, context), equalTo(CodeablePBXSourcesBuildPhase.newInstance(map)));
		}
	}

	@Test
	void throwsExceptionOnUnexpectedIsaValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(new IsaKeyedObject("PBXUnknown"), context));
	}
}
