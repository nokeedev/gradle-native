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

import com.google.common.collect.testing.WrongType;
import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public interface NamedDomainObjectRegistryTester<T> {
	NamedDomainObjectRegistry<T> subject();

	NamedDomainObjectProvider<T> e0();

	NamedDomainObjectProvider<T> e1();

	NamedDomainObjectProvider<T> e2();

	default Class<T> getType() {
		return ProviderUtils.getType(e0())
			.orElseThrow(() -> new RuntimeException("Please implements PolymorphicDomainObjectContainerRegistryTester#getType()."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	default void canRegisterWhenElementAbsent() {
		val result = subject().register("nevu");
		assertNotNull(result);
		assertEquals("nevu", result.getName());
		assertThat(result, providerOf(isA(getType())));
	}

	@Test
	default void canRegisterIfAbsentWhenElementIsAbsent() {
		val result = subject().registerIfAbsent("cera");
		assertNotNull(result);
		assertEquals("cera", result.getName());
		assertThat(result, providerOf(isA(getType())));
	}

	@Test
	default void throwsExceptionOnRegisterWhenElementPresent() {
		assertThrows(RuntimeException.class, () -> subject().register(e0().getName()));
	}

	@Test
	default void doesNotThrowExceptionOnRegisterIfAbsentWhenElementIsPresent() {
		val result = assertDoesNotThrow(() -> subject().registerIfAbsent(e1().getName()));
		assertNotNull(result);
		assertEquals(e1().getName(), result.getName());
		assertThat(result, providerOf(e1().get()));
	}

	@Test
	default void hasRegistrableType() {
		assertNotNull(subject().getRegistrableType());
	}

	interface RegistrableTypeTester {
		NamedDomainObjectRegistry.RegistrableType subject();

		@Test
		default void throwsNullPointerExceptionIfRegisterTypeIsNull() {
			assertThrows(NullPointerException.class, () -> subject().canRegisterType(null));
		}

		@Test
		default void returnsFalseForNonRegistrableType() {
			assertFalse(subject().canRegisterType(WrongType.class));
		}
	}
}
