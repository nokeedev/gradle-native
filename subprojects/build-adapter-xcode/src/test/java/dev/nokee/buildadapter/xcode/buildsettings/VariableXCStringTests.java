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

import com.google.common.collect.ImmutableList;
import dev.nokee.buildadapter.xcode.internal.buildsettings.VariableXCString;
import dev.nokee.buildadapter.xcode.internal.buildsettings.XCString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.anyCallTo;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.callsTo;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.MockitoMethodWrapper.method;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.nullValue;

class VariableXCStringTests {
	@Nested
	class WhenVariableDoesNotExists {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class)
			.when(anyCallTo(XCString.ResolveContext::get).thenReturnNull()).build();
		XCString variable = newMock(XCString.class)
			.when(anyCallTo(XCString::resolve).thenReturn("MY_NULL_VAR"))
			.build();
		VariableXCString subject = new VariableXCString(variable, ImmutableList.of());
		String result = subject.resolve(context);

		@Test
		void returnsNull() {
			assertThat(result, nullValue());
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), calledOnceWith("MY_NULL_VAR"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(method(variable, XCString::resolve), calledOnceWith(context));
		}
	}

	@Nested
	class WhenVariableResolveToNull {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class).build();
		XCString variable = newMock(XCString.class)
			.when(anyCallTo(XCString::resolve).thenReturnNull())
			.build();
		VariableXCString subject = new VariableXCString(variable, ImmutableList.of());
		String result = subject.resolve(context);

		@Test
		void returnsNull() {
			assertThat(result, nullValue());
		}

		@Test
		void doesNotRetrievesResolvedNullVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), neverCalled());
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(method(variable, XCString::resolve), calledOnceWith(context));
		}
	}

	@Nested
	class WhenNoOperations {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class)
			.when(anyCallTo(XCString.ResolveContext::get).thenReturn("my-simple-value")).build();
		XCString variable = newMock(XCString.class)
			.when(anyCallTo(XCString::resolve).thenReturn("MY_VAR"))
			.when(callsTo(XCString::toString).thenReturn("MY_VAR"))
			.build();
		VariableXCString subject = new VariableXCString(variable, ImmutableList.of());
		String result = subject.resolve(context);

		@Test
		void returnsVariableValueWithoutTransformation() {
			assertThat(result, equalTo("my-simple-value"));
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), calledOnceWith("MY_VAR"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(method(variable, XCString::resolve), calledOnceWith(context));
		}

		@Test
		void checkToString() {
			assertThat(subject, hasToString("$(MY_VAR)"));
		}
	}

	@Nested
	class WhenOperations {
		XCString.ResolveContext context = newMock(XCString.ResolveContext.class)
			.when(anyCallTo(XCString.ResolveContext::get).thenReturn("my-value")).build();
		XCString variable = newMock(XCString.class)
			.when(anyCallTo(XCString::resolve).thenReturn("MY_VALUE"))
			.when(callsTo(XCString::toString).thenReturn("MY_VALUE"))
			.build();
		VariableXCString.Operator operator0 = newMock(VariableXCString.Operator.class)
			.when(anyCallTo(VariableXCString.Operator::transform).thenReturn(s -> "prefix-" + s))
			.when(callsTo(VariableXCString.Operator::toString).thenReturn("prefix"))
			.build();
		VariableXCString.Operator operator1 = newMock(VariableXCString.Operator.class)
			.when(anyCallTo(VariableXCString.Operator::transform).thenReturn(s -> s + "-suffix"))
			.when(callsTo(VariableXCString.Operator::toString).thenReturn("suffix"))
			.build();
		VariableXCString subject = new VariableXCString(variable, ImmutableList.of(operator0, operator1));
		String result = subject.resolve(context);

		@Test
		void returnsVariableValueAfterTransformation() {
			assertThat(result, equalTo("prefix-my-value-suffix"));
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(method(context, XCString.ResolveContext::get), calledOnceWith("MY_VALUE"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(method(variable, XCString::resolve), calledOnceWith(context));
		}

		@Test
		void transformsResultUsingOperatorInDeclaredOrder() {
			// Should be in order
			assertThat(method(operator0, VariableXCString.Operator::transform), calledOnceWith("my-value"));
			assertThat(method(operator1, VariableXCString.Operator::transform), calledOnceWith("prefix-my-value"));
		}

		@Test
		void checkToString() {
			assertThat(subject, hasToString("$(MY_VALUE:prefix,suffix)"));
		}
	}
}
