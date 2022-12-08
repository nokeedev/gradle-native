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
package dev.nokee.util.internal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

@ExtendWith(MockitoExtension.class)
class ToOnlyInstanceOfFunctionTests {
	ToOnlyInstanceOfFunction<Number> subject = new ToOnlyInstanceOfFunction<>(Number.class);

	@Nested
	class WhenInputIsNotBaseType {
		@Test
		void returnEmptyStream() {
			assertThat(subject.apply(new Object()).collect(toList()), emptyIterable());
		}
	}

	@Nested
	class WhenInputIsChildType {
		@Test
		void returnStreamWithOnlyInputValue() {
			assertThat(subject.apply(Integer.valueOf("42")).collect(toList()), contains(42));
		}
	}
}
