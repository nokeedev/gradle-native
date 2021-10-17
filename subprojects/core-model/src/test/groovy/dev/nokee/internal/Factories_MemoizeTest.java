/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.internal;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.Factories.constant;
import static dev.nokee.internal.Factories.memoize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

class Factories_MemoizeTest {
	@Test
	void callsDelegateFactoryOnlyOnce() {
		Factory<MyType> delegate = Cast.uncheckedCast(mock(Factory.class));
		val factory = memoize(delegate);
		factory.create();
		factory.create();
		factory.create();
		verify(delegate, times(1)).create();
	}

	@Test
	void alwaysReturnsValueFromDelegateFactory() {
		val factory = memoize(constant(42));
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(memoize(constant(42)), memoize(constant(42)), alreadyCreated(constant(42)))
			.addEqualityGroup(memoize(constant(24)))
			.addEqualityGroup(memoize(constant("foo")))
			.testEquals();
	}

	private static <T> Factory<T> alreadyCreated(Factory<T> delegate) {
		val result = memoize(delegate);
		result.create();
		return result;
	}

	@Test
	void checkToString() {
		assertThat(memoize(constant(42)), hasToString("Factories.memoize(Factories.constant(42))"));
	}

	interface MyType {}
}
