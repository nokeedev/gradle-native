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

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class NoYesBooleanDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);

	@Nested
	class WhenDecodingValidValues {
		NoYesBooleanDecoder subject = new NoYesBooleanDecoder();

		@Nested
		class WhenDecodingNoStringValue {
			Boolean result = subject.decode("NO", context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(false));
			}
		}

		@Nested
		class WhenDecodingYesStringValue {
			Boolean result = subject.decode("YES", context);

			@Test
			void canDecode() {
				assertThat(result, equalTo(true));
			}
		}
	}

	@Nested
	class WhenDecodingInvalidValues {
		TestDouble<ValueDecoder<Boolean, Object>> delegate = newMock(new TypeToken<ValueDecoder<Boolean, Object>>() {})
			.when(any(callTo(method(ValueDecoder<Boolean, Object>::decode))).then(doReturn(false)));
		NoYesBooleanDecoder subject = new NoYesBooleanDecoder(delegate.instance());
		Boolean ignored = subject.decode("bar", context);

		@Test
		void callsDelegateOnUnexpectedValues() {
			assertThat(delegate.to(method(ValueDecoder<Boolean, Object>::decode)), calledOnceWith("bar", context));
		}
	}
}
