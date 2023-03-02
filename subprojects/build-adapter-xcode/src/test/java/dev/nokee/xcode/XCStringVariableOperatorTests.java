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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.internal.testing.testdoubles.MethodVerifier;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doAlwaysReturnNull;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.nullValue;

class XCStringVariableOperatorTests {
	@Nested
	class WhenVariableDoesNotExists {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class)
			.when(any(callTo(method(XCString.ResolveContext::get))).then(doAlwaysReturnNull()));
		TestDouble<XCString> variable = newMock(XCString.class)
			.when(any(callTo(method(XCString::resolve))).then(doReturn("MY_NULL_VAR")));
		XCStringVariableOperator subject = new XCStringVariableOperator(variable.instance(), ImmutableList.of());
		String result = subject.resolve(context.instance());

		@Test
		void returnsNull() {
			assertThat(result, nullValue());
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), calledOnceWith("MY_NULL_VAR"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(variable.to(method(XCString::resolve)), calledOnceWith(context));
		}
	}

	@Nested
	class WhenVariableResolveToNull {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class);
		TestDouble<XCString> variable = newMock(XCString.class) //
			.when(any(callTo(method(XCString::resolve))).then(doAlwaysReturnNull()));
		XCStringVariableOperator subject = new XCStringVariableOperator(variable.instance(), ImmutableList.of());
		String result = subject.resolve(context.instance());

		@Test
		void returnsNull() {
			assertThat(result, nullValue());
		}

		@Test
		void doesNotRetrievesResolvedNullVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), neverCalled());
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(variable.to(method(XCString::resolve)), calledOnceWith(context));
		}
	}

	@Nested
	class WhenNoOperations {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class)
			.when(any(callTo(method(XCString.ResolveContext::get))).then(doReturn("my-simple-value")));
		TestDouble<XCString> variable = newMock(XCString.class)
			.when(any(callTo(method(XCString::resolve))).then(doReturn("MY_VAR")))
			.when(callTo(method(XCString::toString)).then(doReturn("MY_VAR")));
		XCStringVariableOperator subject = new XCStringVariableOperator(variable.instance(), ImmutableList.of());
		String result = subject.resolve(context.instance());

		@Test
		void returnsVariableValueWithoutTransformation() {
			assertThat(result, equalTo("my-simple-value"));
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), calledOnceWith("MY_VAR"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(variable.to(method(XCString::resolve)), calledOnceWith(context));
		}

		@Test
		void checkToString() {
			assertThat(subject, hasToString("$(MY_VAR)"));
		}
	}

	@Nested
	class WhenOperations {
		TestDouble<XCString.ResolveContext> context = newMock(XCString.ResolveContext.class) //
			.when(any(callTo(method(XCString.ResolveContext::get))).then(doReturn("my-value")));
		TestDouble<XCString> variable = newMock(XCString.class) //
			.when(any(callTo(method(XCString::resolve))).then(doReturn("MY_VALUE"))) //
			.when(callTo(method(XCString::toString)).then(doReturn("MY_VALUE")));
		TestDouble<XCStringVariableOperator.Operator> operator0 = newMock(XCStringVariableOperator.Operator.class) //
			.when(any(callTo(method(XCStringVariableOperator.Operator::transform))).then(doReturn((self, i) -> "prefix-" + i.getArgument(0)))) //
			.when(callTo(method(XCStringVariableOperator.Operator::toString)).then(doReturn("prefix")));
		TestDouble<XCStringVariableOperator.Operator> operator1 = newMock(XCStringVariableOperator.Operator.class)
			.when(any(callTo(method(XCStringVariableOperator.Operator::transform))).then(doReturn((self, i) -> i.getArgument(0) + "-suffix"))) //
			.when(callTo(method(XCStringVariableOperator.Operator::toString)).then(doReturn("suffix")));
		XCStringVariableOperator subject = new XCStringVariableOperator(variable.instance(), ImmutableList.of(operator0.instance(), operator1.instance()));
		String result = subject.resolve(context.instance());

		@Test
		void returnsVariableValueAfterTransformation() {
			assertThat(result, equalTo("prefix-my-value-suffix"));
		}

		@Test
		void retrievesResolvedVariableNameFromContext() {
			assertThat(context.to(method(XCString.ResolveContext::get)), calledOnceWith("MY_VALUE"));
		}

		@Test
		void resolvesVariableUsingSameContext() {
			assertThat(variable.to(method(XCString::resolve)), calledOnceWith(context));
		}

		@Test
		void transformsResultUsingOperatorInDeclaredOrder() {
			// Should be in order
			assertThat(operator0.to(method(XCStringVariableOperator.Operator::transform)), calledOnceWith("my-value"));
			assertThat(operator1.to(method(XCStringVariableOperator.Operator::transform)), calledOnceWith("prefix-my-value"));
		}

		@Test
		void checkToString() {
			assertThat(subject, hasToString("$(MY_VALUE:prefix,suffix)"));
		}
	}
}
