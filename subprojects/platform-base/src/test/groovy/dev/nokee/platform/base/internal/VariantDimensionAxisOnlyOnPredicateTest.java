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
package dev.nokee.platform.base.internal;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantDimensionAxisOnlyOnPredicateTest {
	private final VariantDimensionAxisOnlyOnPredicate<MyAxis, MyOtherAxis> subject = new VariantDimensionAxisOnlyOnPredicate<>(MyOtherAxis.V1);

	@Test
	void returnsTrueWhenCurrentAxisIsEmptyAndOtherAxisIsNotExpectedValue() {
		assertTrue(subject.test(Optional.empty(), MyOtherAxis.V0));
	}

	@Test
	void returnsTrueWhenCurrentValueIsNotEmptyAndOtherAxisIsExpectedValue() {
		assertTrue(subject.test(Optional.of(MyAxis.V0), MyOtherAxis.V1));
		assertTrue(subject.test(Optional.of(MyAxis.V1), MyOtherAxis.V1));
	}

	@Test
	void returnsFalseWhenCurrentValueIsNotEmptyAndOtherAxisIsNotExpectedValue() {
		assertFalse(subject.test(Optional.of(MyAxis.V0), MyOtherAxis.V0));
		assertFalse(subject.test(Optional.of(MyAxis.V1), MyOtherAxis.V0));
	}

	@Test
	void returnsFalseWhenCurrentValueIsEmptyAndOtherAxisIsExpectedValue() {
		assertFalse(subject.test(Optional.empty(), MyOtherAxis.V1));
	}

	private enum MyAxis {
		V0, V1;
	}

	private enum MyOtherAxis {
		V0, V1;
	}
}
