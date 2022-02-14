/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.testers;

import com.google.common.testing.NullPointerTester;
import dev.nokee.platform.base.VariantDimensionBuilder;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public interface VariantDimensionBuilderTester<T> {
	VariantDimensionBuilder<T> subject();

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullOnPublicMethods() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	default void returnsSelfFromOnlyOn() {
		assertSame(subject(), subject().onlyOn(mock(MyOtherAxis.class)));
	}

	@Test
	default void returnsSelfFromExceptOn() {
		assertSame(subject(), subject().exceptOn(mock(MyOtherAxis.class)));
	}

	@Test
	@SuppressWarnings("unchecked")
	default void returnsSelfFromOnlyIf() {
		assertSame(subject(), subject().onlyIf(MyOtherAxis.class, mock(BiPredicate.class)));
	}

	@Test
	@SuppressWarnings("unchecked")
	default void returnsSelfFromExceptIf() {
		assertSame(subject(), subject().exceptIf(MyOtherAxis.class, mock(BiPredicate.class)));
	}

	interface MyOtherAxis {}
}
