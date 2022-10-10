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
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectSupplier;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XCProjectSupplierTests {
	@Nested
	class WhenSupplierOfProjects {
		@Mock XCProjectLocator locator;
		Path basePath = Paths.get("/test");
		XCProjectSupplier subject;

		@BeforeEach
		void givenLocateProjectsInSearchBasePath() {
			subject = new XCProjectSupplier(basePath, locator);
			when(locator.findProjects(basePath)).thenReturn(of(project("A"), project("B")));

		}

		@Nested
		class WhenSupplierQueried {
			Iterable<XCProjectReference> result;

			@BeforeEach
			void whenSupplierQueried() {
				result = subject.get();
			}

			@Test
			void returnsFoundProjects() {
				assertThat(result, containsInAnyOrder(project("A"), project("B")));
			}
		}
	}

	@Test
	void canSerialize() {
		XCProjectLocator locator = new TestSerializableXCProjectLocator();

		assertThat(new XCProjectSupplier(Paths.get("/test"), locator), isSerializable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquality() {
		XCProjectLocator locatorA = __ -> { throw new UnsupportedOperationException(); };
		XCProjectLocator locatorB = __ -> { throw new UnsupportedOperationException(); };
		Path basePathA = Paths.get("/testA");
		Path basePathB = Paths.get("/testB");

		new EqualsTester()
			.addEqualityGroup(
				new XCProjectSupplier(basePathA, locatorA),
				new XCProjectSupplier(basePathA, locatorA))
			.addEqualityGroup(new XCProjectSupplier(basePathB, locatorA))
			.addEqualityGroup(new XCProjectSupplier(basePathB, locatorB))
			.testEquals();
	}
}
