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

import java.util.function.UnaryOperator;

import static com.google.common.base.Functions.identity;
import static dev.nokee.internal.Factories.compose;
import static dev.nokee.internal.Factories.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

class Factories_ComposeTest {
	@Test
	void callsComposingFunctionWithFactoryReturnValue() {
		UnaryOperator<MyType> function = Cast.uncheckedCast(mock(UnaryOperator.class));
		val instance = new MyType();
		compose(constant(instance), function).create();
		verify(function, times(1)).apply(instance);
	}

	@Test
	void returnsTransformedValue() {
		assertThat(compose(MyType::new, MyType::toString).create(), equalTo("MyType#toString()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(constant(42), identity()), compose(constant(42), identity()))
			.addEqualityGroup(compose(constant(24), identity()))
			.addEqualityGroup(compose(constant(42), t -> t))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(compose(constant(42), identity()), hasToString("Factories.compose(Factories.constant(42), Functions.identity())"));
	}

	static class MyType {
		@Override
		public String toString() {
			return "MyType#toString()";
		}
	}
}
