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

import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.xcode.project.coders.WrapDecoder.wrap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntegerDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);
	WrapDecoder<Integer> delegate = new WrapDecoder<>();
	IntegerDecoder<WrapDecoder.Wrapper<Integer>> subject = new IntegerDecoder<>(delegate);

	@Nested
	class WhenDecodingIntegerValue {
		WrapDecoder.Wrapper<Integer> result = subject.decode(42, context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(wrap(42)));
		}

		@Test
		void callsDelegateWithIntegerValue() {
			assertThat(delegate, calledOnceWith(42, context));
		}
	}

	@Nested
	class WhenDecodingIntegerValueFromString {
		WrapDecoder.Wrapper<Integer> result = subject.decode("42", context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(wrap(42)));
		}

		@Test
		void callsDelegateWithIntegerValue() {
			assertThat(delegate, calledOnceWith(42, context));
		}
	}

	@Test
	void throwsExceptionOnInvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(new Object(), context));
	}
}
