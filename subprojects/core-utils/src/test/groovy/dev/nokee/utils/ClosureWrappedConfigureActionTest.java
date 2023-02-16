/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.testdoubles.TestClosure;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withClosureArguments;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateFirstStrategy;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateOf;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofClosure;
import static dev.nokee.utils.ClosureTestUtils.doSomething;
import static dev.nokee.utils.ClosureTestUtils.doSomethingElse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClosureWrappedConfigureActionTest {
	private final TestDouble<TestClosure<Void, Object>> closure = newMock(ofClosure(Object.class));
	private final ClosureWrappedConfigureAction<Object> subject = new ClosureWrappedConfigureAction<>(closure.instance());

	@Test
	void usesDelegateFirstStrategy() {
		subject.execute(new Object());
		assertThat(closure.to(method(TestClosure<Void, Object>::execute)), calledOnce(withDelegateFirstStrategy()));
	}

	@Test
	void usesActionArgumentAsDelegate() {
		val value = new Object();
		subject.execute(value);
		assertThat(closure.to(method(TestClosure<Void, Object>::execute)), calledOnce(withDelegateOf(value)));
	}

	@Test
	void usesActionArgumentAsFirstArgument() {
		val value = new Object();
		subject.execute(value);
		assertThat(closure.to(method(TestClosure<Void, Object>::execute)), calledOnce(withClosureArguments(value)));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicConstructors(ClosureWrappedConfigureAction.class);
	}

	@Test
	void canGetConfigureClosure() {
		assertEquals(closure, subject.getConfigureClosure());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val closure = doSomething(Object.class);
		new EqualsTester()
			.addEqualityGroup(new ClosureWrappedConfigureAction<>(closure), new ClosureWrappedConfigureAction<>(closure))
			.addEqualityGroup(new ClosureWrappedConfigureAction<>(doSomethingElse(Object.class)))
			.testEquals();
	}
}
