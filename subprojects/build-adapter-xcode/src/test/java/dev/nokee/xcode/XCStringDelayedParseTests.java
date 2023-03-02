/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class XCStringDelayedParseTests {
	XCString.ResolveContext context = newAlwaysThrowingMock(XCString.ResolveContext.class);
	TestDouble<XCStringParser> parser = newMock(XCStringParser.class) //
		.when(any(callTo(method(XCStringParser::parse))).then(doReturn((self, i) -> XCString.literal(i.getArgument(0)))));
	XCStringDelayedParse subject = new XCStringDelayedParse(parser.instance(), "our-raw-string");

	@Test
	void doesNotParseBeforeResolve() {
		assertThat(parser.to(method(XCStringParser::parse)), neverCalled());
	}

	@Nested
	class WhenResolved {
		String result = subject.resolve(context);

		@Test
		void parsesRawString() {
			assertThat(parser.to(method(XCStringParser::parse)), calledOnceWith("our-raw-string"));
		}

		@Test
		void returnsResolvedParsedString() {
			assertThat(result, equalTo("our-raw-string"));
		}

		@Test
		void doesNotReparseRawStringOnSubsequentResolve() {
			assertThat(subject.resolve(context), equalTo("our-raw-string"));
			assertThat(parser.to(method(XCStringParser::parse)), calledOnce());
		}

		@Test
		void checkToString() {
			assertThat(subject, hasToString("our-raw-string"));
		}
	}

	@Test
	void checkToString() {
		// TODO: parse the string
		assertThat(subject, hasToString("our-raw-string"));
	}
}
