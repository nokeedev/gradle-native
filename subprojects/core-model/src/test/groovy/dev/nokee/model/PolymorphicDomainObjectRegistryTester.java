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
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

public interface PolymorphicDomainObjectRegistryTester<T> {
	PolymorphicDomainObjectRegistry<T> subject();

	NamedDomainObjectProvider<? extends T> e0();

	NamedDomainObjectProvider<? extends T> e1();

	NamedDomainObjectProvider<? extends T> e2();

	<S extends T> NamedDomainObjectProvider<? extends S> e3();

	default Class<? extends T> getType() {
		return ProviderUtils.getType(e0())
			.orElseThrow(() -> new RuntimeException("Please implements PolymorphicDomainObjectContainerRegistryTester#getType()."));
	}

	@SuppressWarnings("unchecked")
	default <S extends T> Class<? extends S> getOtherType() {
		return (Class<? extends S>) ProviderUtils.getType(e3())
			.orElseThrow(() -> new RuntimeException("Please implements PolymorphicDomainObjectContainerRegistryTester#getOtherType()."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	default void canRegisterWhenElementAbsent() {
		val result = subject().register("faxo", getType());
		assertNotNull(result);
		assertEquals("faxo", result.getName());
		assertThat(result, providerOf(isA(getType())));
	}

	@Test
	default void canRegisterIfAbsentWhenElementIsAbsent() {
		val result = subject().registerIfAbsent("qico", getOtherType());
		assertNotNull(result);
		assertEquals("qico", result.getName());
		assertThat(result, providerOf(isA(getOtherType())));
	}

	@Test
	default void throwsExceptionOnRegisterWhenElementPresent() {
		assertThrows(RuntimeException.class, () -> subject().register(e0().getName(), getType()));
	}

	@Test
	default void doesNotThrowExceptionOnRegisterIfAbsentWhenElementIsPresent() {
		val result = assertDoesNotThrow(() -> subject().registerIfAbsent(e3().getName(), getOtherType()));
		assertNotNull(result);
		assertEquals(e3().getName(), result.getName());
		assertThat(result, providerOf(e3().get()));
	}

	@Test
	default void throwsExceptionWhenRegisteringTypeDoesNotMatchExistingElement() {
		assertThrows(RuntimeException.class, () -> subject().registerIfAbsent(e3().getName(), getType()));
	}

	@Test
	default void hasRegistrableTypes() {
		assertNotNull(subject().getRegistrableTypes());
	}

	interface RegistrableTypesTester {
		PolymorphicDomainObjectRegistry.RegistrableTypes subject();

		@Test
		default void throwsNullPointerExceptionIfRegisterTypeIsNull() {
			assertThrows(NullPointerException.class, () -> subject().canRegisterType(null));
		}

		@Test
		default void returnsFalseForNonRegistrableTypes() {
			assertFalse(subject().canRegisterType(WrongType.class));
		}
	}
}
