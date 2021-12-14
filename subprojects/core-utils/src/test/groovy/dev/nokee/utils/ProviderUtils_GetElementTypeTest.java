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

import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.ProviderUtils.getElementType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderUtils_GetElementTypeTest {
	@Test
	void returnsElementTypeFromListProperty() {
		assertThat(getElementType(objectFactory().listProperty(MyType.class)), optionalWithValue(equalTo(MyType.class)));
	}

	@Test
	void returnsElementTypeFromSetProperty() {
		assertThat(getElementType(objectFactory().setProperty(MyType.class)), optionalWithValue(equalTo(MyType.class)));
	}

	@Test
	@SuppressWarnings("unchecked") // ignores unmatched types
	void doesNotReturnElementTypeFromNormalProperty() {
		assertThat(getElementType((Provider) objectFactory().property(MyType.class)), emptyOptional());
	}

	@Test
	void throwsNullPointerExceptionWhenProviderIsNull() {
		assertThrows(NullPointerException.class, () -> ProviderUtils.getElementType(null));
	}

	interface MyType {}
}
