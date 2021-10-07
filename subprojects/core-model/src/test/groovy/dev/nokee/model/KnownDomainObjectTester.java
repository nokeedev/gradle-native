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
package dev.nokee.model;

import com.google.common.reflect.TypeToken;
import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.KnownDomainObjectTestUtils.realize;
import static dev.nokee.utils.ActionTestUtils.mockAction;
import static dev.nokee.utils.ClosureTestUtils.mockClosure;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static dev.nokee.utils.ProviderUtils.fixed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface KnownDomainObjectTester<T> {
	KnownDomainObject<T> subject();

	@Test
	default void hasIdentifier() {
		assertThat(subject().getIdentifier(), notNullValue(DomainObjectIdentifier.class));
	}

	@Test
	default void hasType() {
		assertThat(subject().getType(), is(type()));
	}

	@Test
	default void canMapKnownObject() {
		assertThat(subject().map(it -> "foo"), providerOf("foo"));
	}

	@Test
	default void canFlatMapKnownObject() {
		assertThat(subject().flatMap(it -> fixed("bar")), providerOf("bar"));
	}

	@Test
	default void returnsThisKnownObjectWhenConfiguring() {
		val subject = subject();
		assertAll(
			() -> assertThat(subject.configure(mockAction()), is(subject)),
			() -> assertThat(subject.configure(mockClosure(type())), is(subject))
		);
	}

	@Test
	default void canConfigureKnownObjectUsingAction() {
		val action = ActionTestUtils.mockAction();
		realize(subject().configure(action));
		assertThat(action, calledOnceWith(singleArgumentOf(isA(type()))));
	}

	@Test
	default void canConfigureKnownObjectUsingClosure() {
		val closure = mockClosure(type());
		realize(subject().configure(closure));
		assertThat(closure, calledOnceWith(singleArgumentOf(isA(type()))));
		assertThat(closure, calledOnceWith(allOf(delegateFirstStrategy(), delegateOf(isA(type())))));
	}

	@SuppressWarnings("UnstableApiUsage")
	default Class<? super T> type() {
		return new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}
}
