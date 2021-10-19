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

import com.google.common.collect.testing.WrongType;
import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ActionTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public interface ModelElementTester {
	ModelElement subject();

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	default void hasName() {
		assertNotNull(subject().getName());
	}

	@Test
	default void isNotInstanceOfWrongType() {
		assertFalse(subject().instanceOf(WrongType.class));
	}

	@Test
	default void throwsExceptionWhenConfiguringWrongType() {
		assertThrows(RuntimeException.class, () -> subject().configure(WrongType.class, ActionTestUtils.doSomething()));
	}

	@Test
	default void throwsCastExceptionWhenCastingIntoWrongType() {
		assertThrows(ClassCastException.class, () -> subject().as(WrongType.class));
	}
}
