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

import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TargetFactoryTests {
	@Mock KeyedObject map;
	@InjectMocks TargetFactory<?> subject;

	@Nested
	class WhenIsaPBXAggregateTarget {
		@BeforeEach
		void givenIsaPBXAggregateTarget() {
			when(map.isa()).thenReturn("PBXAggregateTarget");
		}

		@Test
		void createsPBXAggregateTarget() {
			assertThat(subject.create(map), equalTo(CodeablePBXAggregateTarget.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXNativeTarget {
		@BeforeEach
		void givenIsaPBXNativeTarget() {
			when(map.isa()).thenReturn("PBXNativeTarget");
		}

		@Test
		void createsPBXNativeTarget() {
			assertThat(subject.create(map), equalTo(CodeablePBXNativeTarget.newInstance(map)));
		}
	}

	@Nested
	class WhenIsaPBXLegacyTarget {
		@BeforeEach
		void givenIsaPBXLegacyTarget() {
			when(map.isa()).thenReturn("PBXLegacyTarget");
		}

		@Test
		void createsPBXLegacyTarget() {
			assertThat(subject.create(map), equalTo(CodeablePBXLegacyTarget.newInstance(map)));
		}
	}
}
