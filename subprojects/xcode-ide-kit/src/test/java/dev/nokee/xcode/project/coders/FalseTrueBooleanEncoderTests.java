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

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FalseTrueBooleanEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
	FalseTrueBooleanEncoder subject = new FalseTrueBooleanEncoder();

	@Nested
	class WhenEncodingTrueValue {
		Boolean result = subject.encode(true, context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(true));
		}
	}

	@Nested
	class WhenEncodingFalseValue {
		Boolean result = subject.encode(false, context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(false));
		}
	}
}
