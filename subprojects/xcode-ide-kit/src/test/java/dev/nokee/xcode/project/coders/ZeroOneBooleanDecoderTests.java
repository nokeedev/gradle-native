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
import dev.nokee.xcode.utils.ConstantDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ZeroOneBooleanDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);

	@Nested
	class WhenDecodingValidValues {
		ZeroOneBooleanDecoder subject = new ZeroOneBooleanDecoder();

		@Nested
		class WhenDecodingZeroIntegerValue {
			Boolean result = subject.decode(0, context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(false));
			}
		}

		@Nested
		class WhenDecodingZeroStringValue {
			Boolean result = subject.decode("0", context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(false));
			}
		}

		@Nested
		class WhenDecodingOneIntegerValue {
			Boolean result = subject.decode(1, context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(true));
			}
		}

		@Nested
		class WhenDecodingOneStringValue {
			Boolean result = subject.decode("1", context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(true));
			}
		}
	}

	@Nested
	class WhenDecodingInvalidValues {
		ConstantDecoder<Boolean, Object> delegate = new ConstantDecoder<>(true);
		NoYesBooleanDecoder subject = new NoYesBooleanDecoder(delegate);
		Boolean ignored = subject.decode("far", context);

		@Test
		void callsDelegateOnUnexpectedValues() {
			assertThat(delegate, calledOnceWith("far", context));
		}
	}
}
