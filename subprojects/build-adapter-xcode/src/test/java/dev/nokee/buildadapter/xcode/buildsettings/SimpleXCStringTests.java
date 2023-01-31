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
package dev.nokee.buildadapter.xcode.buildsettings;

import dev.nokee.buildadapter.xcode.internal.buildsettings.SimpleXCString;
import dev.nokee.buildadapter.xcode.internal.buildsettings.XCString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.anyCallTo;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.MockitoMethodWrapper.method;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class SimpleXCStringTests {
	SimpleXCString subject = new SimpleXCString("VARIABLE_NAME");

	@Nested
	class WhenVariableDoesNotExists {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class) //
			.when(anyCallTo(XCString.ResolveContext::get).thenReturnNull()) //
			.build();
		String result = subject.resolve(context);

		@Test
		void returnsVariableAsLiteral() {
			assertThat(result, equalTo("$VARIABLE_NAME"));
		}

		@Test
		void retrievesVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), calledOnceWith("VARIABLE_NAME"));
		}
	}

	@Nested
	class WhenResolved {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class) //
			.when(anyCallTo(XCString.ResolveContext::get).thenReturn("my-result")) //
			.build();
		String result = subject.resolve(context);

		@Test
		void resolvesVariableAgainstContext() {
			assertThat(result, equalTo("my-result"));
		}

		@Test
		void retrievesVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), calledOnceWith("VARIABLE_NAME"));
		}
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("$VARIABLE_NAME"));
	}
}
