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
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.asSupplier;
import static dev.nokee.internal.Factories.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(Factories.class)
class Factories_AsSupplierTest {
	@Test
	void callsFactoryOnGet() {
		Factory<Object> factory = Cast.uncheckedCast(mock(Factory.class));
		asSupplier(factory).get();
		asSupplier(factory).get();
		asSupplier(factory).get();
		verify(factory, times(3)).create();
	}

	@Test
	void returnsCreatedValue() {
		assertThat(asSupplier(constant(42)).get(), equalTo(42));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(asSupplier(constant(42)), asSupplier(constant(42)))
			.addEqualityGroup(asSupplier(constant(24)))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(asSupplier(constant(42)), hasToString("Factories.asSupplier(Factories.constant(42))"));
	}
}
