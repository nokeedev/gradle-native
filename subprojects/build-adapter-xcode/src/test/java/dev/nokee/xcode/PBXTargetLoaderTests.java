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

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PBXTargetLoaderTests {
	@Mock XCLoader<PBXProject, XCProjectReference> loader;
	@InjectMocks PBXTargetLoader subject;

	@Nested
	class WhenPBXProjectHasTargetsWithRequestedReference {
		PBXTarget result;
		PBXTarget target0 = PBXAggregateTarget.builder().lenient().name("Foo").build();
		PBXTarget target1 = PBXAggregateTarget.builder().lenient().name("Bar").build();
		PBXTarget target2 = PBXAggregateTarget.builder().lenient().name("Far").build();

		@BeforeEach
		void givenPBXProjectWithRequestedTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder().targets(target0, target1, target2).build());
			result = subject.load(project("MyProject").ofTarget("Bar"));
		}

		@Test
		void returnsTargetMatchingTargetReferenceName() {
			assertThat(result, is(target1));
		}
	}

	@Nested
	class WhenPBXProjectHasNoTarget {
		@BeforeEach
		void givenPBXProjectWithNoTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder().build());
		}

		@Test
		void throwsException() {
			assertThrows(RuntimeException.class, () -> subject.load(project("MyProject").ofTarget("Bar")));
		}
	}

	@Nested
	class WhenPBXProjectHasTargetsButNoneMatchReference {
		PBXTarget target0 = PBXAggregateTarget.builder().lenient().name("Foo").build();
		PBXTarget target1 = PBXAggregateTarget.builder().lenient().name("Far").build();


		@BeforeEach
		void givenPBXProjectWithTargetsWithoutRequestedTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder().targets(target0, target1).build());
		}

		@Test
		void throwsException() {
			assertThrows(RuntimeException.class, () -> subject.load(project("MyProject").ofTarget("Bar")));
		}
	}
}
