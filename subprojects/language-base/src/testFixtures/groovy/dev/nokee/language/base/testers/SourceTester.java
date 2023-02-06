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
package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.testdoubles.TestClosure;
import dev.nokee.language.base.LanguageSourceSet;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withClosureArguments;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateFirstStrategy;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateOf;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofAction;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofClosure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface SourceTester<T, U> {
	T subject();

	NamedDomainObjectProvider<? extends LanguageSourceSet> get(T self);
	void configure(T self, Action<? super U> action);
	void configure(T self, @SuppressWarnings("rawtypes") Closure closure);


	default LanguageSourceSet sourceSetUnderTest() {
		return get(subject()).get();
	}

	@Test
	default void canAccessSourceUsingTypeSafeAccessor() {
		assertThat(get(subject()), isA(NamedDomainObjectProvider.class));
	}

	//region
	@Test
	default void canConfigureSourceViaTypeSafeMethodUsingAction() {
		val action = newMock(ofAction(Object.class));
		configure(subject(), action.instance());
		assertThat(action.to(method(Action<Object>::execute)), calledOnceWith(sourceSetUnderTest()));
	}

	@Test
	default void canConfigureSourceViaTypeSafeMethodUsingClosure() {
		val closure = newMock(ofClosure(LanguageSourceSet.class));
		configure(subject(), closure.instance());
		assertAll(
			() -> assertThat(closure.to(method(TestClosure<Object, LanguageSourceSet>::execute)), calledOnce(withClosureArguments(sourceSetUnderTest()))),
			() -> assertThat(closure.to(method(TestClosure<Object, LanguageSourceSet>::execute)), calledOnce(allOf(withDelegateOf(sourceSetUnderTest()), withDelegateFirstStrategy())))
		);
	}
	//endregion
}
