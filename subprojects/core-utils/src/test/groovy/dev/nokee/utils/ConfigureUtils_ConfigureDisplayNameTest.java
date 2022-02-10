/*
 * Copyright 2020-2021 the original author or authors.
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
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;
import static dev.nokee.utils.ConfigureUtils.property;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigureUtils_ConfigureDisplayNameTest {
	@Test
	void canConfigureDisplayNameOfProperty() {
		val property = objectFactory().property(String.class);
		configureDisplayName(property, property("foo"));
		val ex = assertThrows(RuntimeException.class, () -> property.value((String) null).get());
		assertThat(ex.getMessage(), containsString("property 'foo'"));
	}

	@Test
	void canConfigureDisplayNameOfSetProperty() {
		val property = objectFactory().setProperty(String.class);
		configureDisplayName(property, property("foo"));
		val ex = assertThrows(RuntimeException.class, () -> property.value((Iterable<String>) null).get());
		assertThat(ex.getMessage(), containsString("property 'foo'"));
	}

	@Test
	void canConfigureDisplayNameOfListProperty() {
		val property = objectFactory().listProperty(String.class);
		configureDisplayName(property, property("foo"));
		val ex = assertThrows(RuntimeException.class, () -> property.value((Iterable<String>) null).get());
		assertThat(ex.getMessage(), containsString("property 'foo'"));
	}

	@Test
	void canConfigureDisplayNameOfFileProperty() {
		val property = objectFactory().fileProperty();
		configureDisplayName(property, property("foo"));
		val ex = assertThrows(RuntimeException.class, () -> property.fileValue(null).get());
		assertThat(ex.getMessage(), containsString("property 'foo'"));
	}

	@Test
	void canConfigureDisplayNameOfDirectoryProperty() {
		val property = objectFactory().directoryProperty();
		configureDisplayName(property, property("foo"));
		val ex = assertThrows(RuntimeException.class, () -> property.fileValue(null).get());
		assertThat(ex.getMessage(), containsString("property 'foo'"));
	}

	@Test
	void returnsTheProperty() {
		val property = objectFactory().property(String.class);
		assertSame(property, configureDisplayName(property, "foo"));
	}

	@Test
	void returnsTheSetProperty() {
		val property = objectFactory().setProperty(String.class);
		assertSame(property, configureDisplayName(property, "foo"));
	}

	@Test
	void returnsTheListProperty() {
		val property = objectFactory().listProperty(String.class);
		assertSame(property, configureDisplayName(property, "foo"));
	}

	@Test
	void returnsTheFileProperty() {
		val property = objectFactory().fileProperty();
		assertSame(property, configureDisplayName(property, "foo"));
	}

	@Test
	void returnsTheDirectoryProperty() {
		val property = objectFactory().directoryProperty();
		assertSame(property, configureDisplayName(property, "foo"));
	}
}
