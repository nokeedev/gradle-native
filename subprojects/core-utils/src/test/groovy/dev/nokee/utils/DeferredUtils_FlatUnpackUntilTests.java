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

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.DeferredUtils.flatUnpackUntil;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DeferredUtils_FlatUnpackUntilTests {
	@Nested
	class GivenSubject {
		DeferredUtils.Executable<List<MyType>> subject = flatUnpackUntil(MyType.class);

		@Nested
		class WhenTargetIsNull {
			@Test
			void returnsEmptyList() {
				assertThat(subject.execute(null), emptyIterable());
			}
		}

		@Nested
		class WhenTargetIsEmptyLists {
			@Test
			void returnsEmptyList() {
				assertThat(subject.execute(of()), emptyIterable());
			}

			@Test
			void flattenToSingleEmptyList() {
				assertThat(subject.execute(of(of())), emptyIterable());
			}


			@Test
			void flattenToOnlySingleEmptyList() {
				assertThat(subject.execute(of(of(of()))), emptyIterable());
			}
		}

		@Nested
		class WhenTargetListsOfType {
			@Mock
			MyType value;

			@Test
			void returnsSingleList() {
				assertThat(subject.execute(of(value)), contains(value));
			}

			@Test
			void flattenToSingleList() {
				assertThat(subject.execute(of(of(value))), contains(value));
			}

			@Test
			void flattenToOnlySingleList() {
				assertThat(subject.execute(of(of(of(value)))), contains(value));
			}
		}

		@Test
		void flattenAllListToSingleList() {
			MyType a = mock(MyType.class);
			MyType b = mock(MyType.class);
			MyType c = mock(MyType.class);
			MyType d = mock(MyType.class);
			MyType e = mock(MyType.class);
			MyType f = mock(MyType.class);
			MyType g = mock(MyType.class);

			assertThat(subject.execute(of(of(a, b), of(c, of(d), of(e)), of(f, g))),
				contains(a, b, c, d, e, f, g));
		}
	}

	@Test
	void doesNotFlattenTargetIterableType() {
		val subject = mock(MockUpConfiguration.class);
		assertThat(flatUnpackUntil(MockUpConfiguration.class).execute(subject), contains(subject));
	}

	interface MockUpConfiguration extends Iterable<File> {}

	interface MyType {}
}
