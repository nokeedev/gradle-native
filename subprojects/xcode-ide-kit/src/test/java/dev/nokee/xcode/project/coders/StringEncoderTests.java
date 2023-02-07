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

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.xcode.project.coders.UnwrapEncoder.wrap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class StringEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
	UnwrapEncoder<String> delegate = new UnwrapEncoder<>();
	StringEncoder<UnwrapEncoder.Wrapper<String>> subject = new StringEncoder<>(delegate);

	@Nested
	class WhenEncodingValue {
		Object result = subject.encode(wrap("value"), context);

		@Test
		void canEncode() {
			assertThat(result, equalTo("value"));
		}

		@Test
		void callsDelegateWithInputValue() {
			assertThat(delegate, calledOnceWith(wrap("value"), context));
		}
	}
}
