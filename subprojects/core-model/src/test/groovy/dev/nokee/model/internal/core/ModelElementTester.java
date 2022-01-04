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
package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public interface ModelElementTester {
	ModelElement subject();

	ModelType<?> aKnownType();

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullsOnPublicMethods() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicInstanceMethods(subject());
	}

	@Test
	default void hasName() {
		assertNotNull(subject().getName());
	}

	@Test
	default void isInstanceOfKnownType() {
		assertTrue(subject().instanceOf(aKnownType()));
	}

	@Test
	default void isNotInstanceOfWrongType() {
		assertFalse(subject().instanceOf(of(WrongType.class)));
	}

	@Test
	default void doesNotThrowWhenConfigureUsingActionOfKnownType() {
		assertDoesNotThrow(() -> subject().configure(aKnownType(), doSomething()));
	}

	@Test
	default void throwsExceptionWhenConfigureUsingActionOfWrongType() {
		assertThrows(RuntimeException.class, () -> subject().configure(of(WrongType.class), doSomething()));
	}

	@Test
	default void returnsThisModelElementOnConfigureUsingAction() {
		assertSame(subject(), subject().configure(aKnownType(), doSomething()));
	}

	@Test
	default void throwsExceptionWhenConfiguringWrongType() {
		assertThrows(RuntimeException.class, () -> subject().configure(WrongType.class, doSomething()));
	}

	@Test
	default void throwsCastExceptionWhenCastingIntoWrongType() {
		val ex = assertThrows(ClassCastException.class, () -> subject().as(WrongType.class));
		assertThat("starts with meaningful message", ex.getMessage(), startsWith("Could not cast"));
		assertThat("mention target simple type", ex.getMessage(), containsString(" to WrongType."));
		assertThat("mention castable types", ex.getMessage(), allOf(containsString("Available instances: "), containsString(aKnownType().getConcreteType().getSimpleName())));
	}

	@Test
	default void mixinTypeBecomesKnown() {
		subject().mixin(of(MixInType.class));
		assertTrue(subject().instanceOf(of(MixInType.class)));
	}

	@Test
	default void throwsExceptionWhenMixInKnownType() {
		assertThrows(RuntimeException.class, () -> subject().mixin(aKnownType()));
	}

	@Test
	default void throwsExceptionWhenMixInSuperTypeOfKnownType() {
		assertDoesNotThrow(() -> subject().mixin(of(MixInChildType.class)));
		assertThrows(RuntimeException.class, () -> subject().mixin(of(MixInType.class)));
	}

	interface WrongType {}
	interface MixInType {}
	interface MixInChildType extends MixInType {}
}
