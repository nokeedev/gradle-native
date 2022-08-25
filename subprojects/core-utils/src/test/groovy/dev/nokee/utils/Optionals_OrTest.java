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
package dev.nokee.utils;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Supplier;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.utils.Optionals.or;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

class Optionals_OrTest {
	@Test
	void returnsSuppliedOptionalWhenSelfOptionalIsAbsent() {
		assertThat(or(empty(), () -> of("or-value")), optionalWithValue(equalTo("or-value")));
	}

	@Test
	void returnsSelfOptionalWhenPresent() {
		assertThat(or(of("present"), () -> of("or-value")), optionalWithValue(equalTo("present")));
	}

	@Test
	void doesNotGetSupplierWhenSelfOptionalIsPresent() {
		@SuppressWarnings("unchecked")
		Supplier<Optional<Integer>> supplier = Mockito.mock(Supplier.class);
		or(of("some-value"), supplier);
		Mockito.verify(supplier, never()).get();
	}

	@Test
	void throwsExceptionWhenSupplierReturnsNull() {
		assertThrows(RuntimeException.class, () -> or(empty(), () -> null));
	}

	@Test
	void checkNulls() {
		assertThrows(NullPointerException.class, () -> or(null, () -> of("some-other-value")));
		assertThrows(NullPointerException.class, () -> or(of("my-value"), null));
	}
}
