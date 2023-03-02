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

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doAlwaysReturnNull;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class XCStringVariableTests {
	XCStringVariable subject = new XCStringVariable("VARIABLE_NAME");

	@Nested
	class WhenVariableDoesNotExists {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class) //
			.when(any(callTo(method(XCString.ResolveContext::get))).then(doAlwaysReturnNull()));
		String result = subject.resolve(context.instance());

		@Test
		void returnsVariableAsLiteral() {
			assertThat(result, equalTo("$VARIABLE_NAME"));
		}

		@Test
		void retrievesVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), calledOnceWith("VARIABLE_NAME"));
		}
	}

	@Nested
	class WhenResolved {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class) //
			.when(any(callTo(method(XCString.ResolveContext::get))).then(doReturn("my-result")));
		String result = subject.resolve(context.instance());

		@Test
		void resolvesVariableAgainstContext() {
			assertThat(result, equalTo("my-result"));
		}

		@Test
		void retrievesVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), calledOnceWith("VARIABLE_NAME"));
		}
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("$VARIABLE_NAME"));
	}
}
