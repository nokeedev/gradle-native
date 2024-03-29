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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.ValueEncoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.called;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.with;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.xcode.project.coders.UnwrapEncoder.wrap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class ListEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
	UnwrapEncoder<String> delegate = new UnwrapEncoder<>();
	ListEncoder<String, UnwrapEncoder.Wrapper<String>> subject = new ListEncoder<>(delegate);

	@Nested
	class WhenEncodingEmptyList {
		List<String> result = subject.encode(of(), context);

		@Test
		void returnsEmptyList() {
			assertThat(result, emptyIterable());
		}

		@Test
		void doesNotCallDelegate() {
			assertThat(delegate, neverCalled());
		}
	}

	@Nested
	class WhenEncodingListWithElements {
		List<String> result = subject.encode(of(wrap("first"), wrap("second"), wrap("third")), context);

		@Test
		void returnsEncodedList() {
			assertThat(result, contains("first", "second", "third"));
		}

		@Test
		void callsDelegateWithEachListElementsInOrder() {
			assertThat(delegate, called(with(wrap("first"), context), with(wrap("second"), context), with(wrap("third"), context)));
		}
	}
}
