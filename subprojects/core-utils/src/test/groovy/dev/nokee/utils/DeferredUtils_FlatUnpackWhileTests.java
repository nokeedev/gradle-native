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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.DeferredUtils.flatUnpackWhile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeferredUtils_FlatUnpackWhileTests {

	@Nested
	class GivenSubject {
		@Mock Predicate<Object> predicate;
		DeferredUtils.Executable<List<MyType>> subject;

		@BeforeEach
		void createSubject() {
			subject = flatUnpackWhile(predicate);
		}

		@Nested
		class WhenTargetIsNull {
			@Test
			void returnsEmptyList() {
				assertThat(subject.execute(null), emptyIterable());
			}
		}

		@Nested
		class WhenTargetIsNestedLists {
			@Mock MyType value;

			@Test
			void returnsEmptyList() {
				when(predicate.test(any())).thenReturn(true);
				when(predicate.test(value)).thenReturn(false);
				assertThat(subject.execute(of(value)), contains(value));
			}

			@Test
			void flattenToSingleEmptyList() {
				when(predicate.test(any())).thenReturn(true);
				when(predicate.test(value)).thenReturn(false);
				assertThat(subject.execute(of(value, of(value), value)), contains(value, value, value));
			}


			@Test
			void flattenToOnlySingleEmptyList() {
				when(predicate.test(any())).thenReturn(true);
				when(predicate.test(value)).thenReturn(false);
				assertThat(subject.execute(of(value, of(of(value), value), value)), contains(value, value, value, value));
			}
		}
	}

	interface MyType {}
}
