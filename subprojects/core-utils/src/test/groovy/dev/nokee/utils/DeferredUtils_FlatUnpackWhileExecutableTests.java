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

import dev.nokee.util.Unpacker;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeferredUtils_FlatUnpackWhileExecutableTests {
	@Test
	void createsFlatUnpackWhileExecutableUsingDefaultFlattenerAndUnpackerWithUntilCondition() {
		assertThat(DeferredUtils.flatUnpackUntil(MyType.class),
				equalTo(new DeferredUtils.FlatUnpackWhileExecutable<>(DeferredUtils.DEFAULT_FLATTENER, DeferredUtils.DEFAULT_UNPACKER, not(instanceOf(MyType.class)))));
	}

	@Test
	void createsFlatUnpackWhileExecutableUsingDefaultFlattenerAndUnpackerWithSpecifiedPredicate() {
		@SuppressWarnings("unchecked")
		Predicate<Object> predicate = (Predicate<Object>) mock(Predicate.class);
		assertThat(DeferredUtils.flatUnpackWhile(predicate),
				equalTo(new DeferredUtils.FlatUnpackWhileExecutable<>(DeferredUtils.DEFAULT_FLATTENER, DeferredUtils.DEFAULT_UNPACKER, predicate)));
	}

	@Nested
	class GivenSubject {
		@Mock DeferredUtils.Flattener flattener;
		@Mock Unpacker unpacker;
		@Mock Predicate<Object> predicate;
		@InjectMocks DeferredUtils.FlatUnpackWhileExecutable<Object> subject;

		@Test
		void whenInputIsNull() {
			assertThat(subject.execute(null), emptyIterable());
		}

		@Test
		void whenInputIsTarget() {
			val input = new Object();
			when(predicate.test(input)).thenReturn(false);
			assertThat(subject.execute(input), contains(input));
			verify(unpacker, never()).unpack(any());
			verify(flattener, never()).flatten(any());
		}

		@Test
		void whenInputNestedListToTarget() {
			val input = new Object();
			val unpackedInput = new Object();
			val output = new Object();
			val flattenInput = of(output);

			when(predicate.test(any())).thenReturn(true, true, false);
			when(unpacker.unpack(any())).thenReturn(unpackedInput);
			when(flattener.flatten(any())).thenReturn(flattenInput);

			assertThat(subject.execute(input), contains(output));

			val inOrder = Mockito.inOrder(unpacker, flattener, predicate);
			inOrder.verify(predicate).test(input);
			inOrder.verify(unpacker).unpack(input);
			inOrder.verify(predicate).test(unpackedInput);
			inOrder.verify(flattener).flatten(unpackedInput);
			inOrder.verify(predicate).test(output);
		}
	}

	interface MyType {}
}
