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

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import org.gradle.api.Named;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.BiPredicate;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.platform.base.internal.DefaultBuildVariant.of;
import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VariantDimensionAxisFilterTest {
	@SuppressWarnings("unchecked")
	private final BiPredicate<Optional<MyAxis>, MyOtherAxis> predicate = mock(BiPredicate.class);
	private final VariantDimensionAxisFilter<MyAxis, MyOtherAxis> subject = new VariantDimensionAxisFilter<>(MyAxis.axis(), MyOtherAxis.class, predicate);
	@SuppressWarnings("unchecked")
	private final ArgumentCaptor<Optional<MyAxis>> firstArgument = ArgumentCaptor.forClass(Optional.class);

	abstract class ForwardsPredicateResultThroughPredicateTester {
		abstract BuildVariantInternal buildVariant();

		@Test
		void returnsTrueWhenActionReturnsTrue() {
			when(predicate.test(any(), any())).thenReturn(true);
			assertTrue(subject.test(buildVariant()));
		}

		@Test
		void returnsFalseWhenActionReturnsFalse() {
			when(predicate.test(any(), any())).thenReturn(false);
			assertFalse(subject.test(buildVariant()));
		}
	}

	@Nested
	class WhenCurrentCoordinateIsAbsentTest extends ForwardsPredicateResultThroughPredicateTester {
		private final BuildVariantInternal buildVariant = of(absentCoordinate(MyAxis.axis()), MyOtherAxis.INSTANCE);

		@Override
		BuildVariantInternal buildVariant() {
			return buildVariant;
		}

		@BeforeEach
		void setUp() {
			when(predicate.test(any(), any())).thenReturn(true);
		}

		@Test
		void callsActionWithEmptyOptional() {
			subject.test(buildVariant);
			verify(predicate).test(firstArgument.capture(), any());
			assertThat(firstArgument.getValue(), emptyOptional());
		}

		@Test
		void callsActionWithOtherAxisValue() {
			subject.test(buildVariant);
			verify(predicate).test(any(), eq(MyOtherAxis.INSTANCE));
		}
	}

	@Nested
	class WhenCurrentCoordinateIsPresentTest extends ForwardsPredicateResultThroughPredicateTester {
		private final BuildVariantInternal buildVariant = of(MyAxis.INSTANCE, MyOtherAxis.INSTANCE);

		@Override
		BuildVariantInternal buildVariant() {
			return buildVariant;
		}

		@BeforeEach
		void setUp() {
			when(predicate.test(any(), any())).thenReturn(true);
		}

		@Test
		void callsActionWithOptionalOfCurrentValue() {
			subject.test(buildVariant);
			verify(predicate).test(firstArgument.capture(), any());
			assertThat(firstArgument.getValue(), optionalWithValue(equalTo(MyAxis.INSTANCE)));
		}

		@Test
		void callsActionWithOtherAxisValue() {
			subject.test(buildVariant);
			verify(predicate).test(any(), eq(MyOtherAxis.INSTANCE));
		}
	}

	@Nested
	class WhenOtherCoordinateIsAbsentTest {
		private final boolean result = subject.test(of(MyAxis.INSTANCE, absentCoordinate(MyOtherAxis.axis())));

		@Test
		void doesNotCallAction() {
			verify(predicate, never()).test(any(), any());
		}

		@Test
		void testsReturnsTrue() {
			assertTrue(result);
		}
	}

	@Nested
	class WhenOtherCoordinateIsNotBuildVariantCoordinateTest {
		private final boolean result = subject.test(of(MyAxis.INSTANCE, MyUnrelatedAxis.INSTANCE));

		@Test
		void doesNotCallAction() {
			verify(predicate, never()).test(any(), any());
		}

		@Test
		void testsReturnsTrue() {
			assertTrue(result);
		}
	}

	enum MyAxis implements Named, Coordinate<MyAxis> {
		INSTANCE {
			@Override
			public String getName() {
				return "myAxis";
			}
		};

		public static CoordinateAxis<MyAxis> axis() {
			return INSTANCE.getAxis();
		}
	}
	enum MyOtherAxis implements Named, Coordinate<MyOtherAxis> {
		INSTANCE {
			@Override
			public String getName() {
				return "myOtherAxis";
			}
		};

		public static CoordinateAxis<MyOtherAxis> axis() {
			return INSTANCE.getAxis();
		}
	}
	enum MyUnrelatedAxis implements Named, Coordinate<MyUnrelatedAxis> {
		INSTANCE {
			@Override
			public String getName() {
				return "myUnrelatedAxis";
			}
		}
	}
}
