/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.getElementType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NamedDomainObjectCollectionUtils_GetElementTypeTest {
	@Test
	void canGetTypeOnTaskContainer() {
		val type = assertDoesNotThrow(() -> getElementType(rootProject().getTasks()));
		assertThat(type, is(Task.class));
	}

	@Test
	void canGetTypeOnNamedContainer() {
		val container = objectFactory().domainObjectContainer(Bean.class);
		val type = assertDoesNotThrow(() -> getElementType(container));
		assertThat(type, is(Bean.class));
	}

	@Test
	void canGetTypeOnPolymorphicContainer() {
		val container = objectFactory().polymorphicDomainObjectContainer(Bean.class);
		val type = assertDoesNotThrow(() -> getElementType(container));
		assertThat(type, is(Bean.class));
	}

	interface Bean extends Named {}
}
