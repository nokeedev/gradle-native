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
package dev.nokee.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import dev.nokee.model.internal.core.Bit;
import dev.nokee.model.internal.core.Bits;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Streams.stream;
import static dev.nokee.model.internal.core.Bit.One;
import static dev.nokee.model.internal.core.Bit.Zero;
import static dev.nokee.model.internal.core.Bits.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface BitsTester {
	Bits subject();

	Bits unsetBits();

	Bits setBits();

	@BeforeEach
	default void checkAssumptions() {
		// TODO: setBits() should not be subject() but has some overlapping bits with subject()
		assertNotEquals(subject(), setBits(), "requires a set of bits that partially overlap subject");
		// TODO: unsetBits() should not be empty() and has no overlapping bits with subject()
	}

	@Test
	default void alwaysReturnsEmptyBitsOnBitwiseAndOperationInvolvingEmptyBits() {
		assertEquals(empty(), subject().and(empty()));
		assertEquals(empty(), empty().and(subject())); // for commutativity
	}

	@Test
	default void alwaysReturnsSelfOnBitwiseAndOperationWithSelf() {
		assertEquals(subject(), subject().and(subject()));
	}

	@Test
	default void alwaysReturnsSelfOnBitwiseOrOperatingWithSelf() {
		assertEquals(subject(), subject().or(subject()));
	}

	@Test
	default void alwaysReturnsSelfOnBitwiseOrOperatingWithEmptyBits() {
		assertEquals(subject(), subject().or(empty()));
		assertEquals(subject(), empty().or(subject())); // for commutativity
	}

	@Test
	default void throwsExceptionOnRightShiftOfNegativeValue() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject().rightShift(-42));
		assertEquals("Bit shift 'count' (-42) must be positive.", ex.getMessage());
	}

	@Test
	default void hasSameNumberOfBitsAsLength() {
		assertThat(subject(), iterableWithSize(subject().length()));
	}

	@Test
	default void throwsExceptionOnBitwiseAndOperationAgainstNull() {
		assertThrows(NullPointerException.class, () -> subject().and(null));
	}

	@Test
	default void throwsExceptionOnBitwiseOrOperationAgainstNull() {
		assertThrows(NullPointerException.class, () -> subject().or(null));
	}

	@Test
	default void alwaysReturnsEmptyBitsOnRightShiftByLength() {
		assertEquals(empty(), subject().rightShift(subject().length()));
	}

	@Test
	default void hasOneBitInLastIterablePositionToRepresentMostSignificantBit__AssumingNotEmptyBits() {
		Assumptions.assumeFalse(subject().equals(empty()), "empty bits has no set bits");
		assertEquals(One, getLast(subject()));
	}

	@Test
	default void returnsEmptyBitsOnBitwiseAndOperationAgainstUnsetBits() {
		assertThat(subject(), hasNoneOfTheBitsSet(unsetBits()));

		assertEquals(empty(), subject().and(unsetBits()));
		assertEquals(empty(), unsetBits().and(subject())); // for commutativity
	}

	@Test
	default void returnsCommonBitsOnBitwiseAndOperationAgainstSetBits() {
		assertThat(subject(), not(equalTo(setBits())));
		assertThat(subject(), not(hasNoneOfTheBitsSet(setBits())));

		assertThat(subject().and(setBits()), contains(allCommonSetBitsOf(subject(), setBits())));
		assertThat(setBits().and(subject()), contains(allCommonSetBitsOf(subject(), setBits()))); // for commutativity
	}

	@Test
	default void returnsCombinationOfAllSetBitsOnBitwiseOrOperationAgainstUnsetBits() {
		assertThat(subject(), hasNoneOfTheBitsSet(unsetBits()));

		assertThat(subject().or(unsetBits()), contains(allSetBitsOf(subject(), unsetBits())));
		assertThat(unsetBits().or(subject()), contains(allSetBitsOf(subject(), unsetBits()))); // for commutativity
	}

	static Bit[] allSetBitsOf(Iterable<Bit> leftBits, Iterable<Bit> rightBits) {
		val left = Lists.newArrayList(leftBits);
		val right = Lists.newArrayList(rightBits);
		val diffSize = left.size() - right.size();
		if (diffSize > 0) {
			for (int i = 0; i < diffSize; ++i) {
				right.add(Zero);
			}
		} else {
			for (int i = 0; i < -diffSize; ++ i) {
				left.add(Zero);
			}
		}
		return Streams.zip(left.stream(), right.stream(), (l, r) -> {
			if (l.isSet() || r.isSet()) {
				return One;
			} else {
				return Zero;
			}
		}).toArray(Bit[]::new);
	}

	static Bit[] allCommonSetBitsOf(Iterable<Bit> leftBits, Iterable<Bit> rightBits) {
		val left = Lists.newArrayList(leftBits);
		val right = Lists.newArrayList(rightBits);
		val diffSize = left.size() - right.size();
		if (diffSize > 0) {
			for (int i = 0; i < diffSize; ++i) {
				right.add(Zero);
			}
		} else {
			for (int i = 0; i < -diffSize; ++ i) {
				left.add(Zero);
			}
		}
		return Streams.zip(left.stream(), right.stream(), (l, r) -> {
			if (l.isSet() && r.isSet()) {
				return One;
			} else {
				return Zero;
			}
		}).toArray(Bit[]::new);
	}

	@Test
	default void alwaysReturnsSelfOnRightShiftOfZeroBit() {
		assertEquals(subject(), subject().rightShift(0));
	}

	@Test
	default void canShiftOneBitsToTheRight() {
		Assumptions.assumeTrue(subject().length() > 1);
		assertThat(subject().rightShift(1), contains(stream(skip(subject(), 1)).toArray(Bit[]::new)));
	}

	@Test
	default void canShiftThreeBitsToTheRight() {
		Assumptions.assumeTrue(subject().length() > 3);
		assertThat(subject().rightShift(3), contains(stream(skip(subject(), 3)).toArray(Bit[]::new)));
	}

	@Test
	default void canShiftTwentyBitsToTheRight() {
		Assumptions.assumeTrue(subject().length() > 20);
		assertThat(subject().rightShift(20), contains(stream(skip(subject(), 20)).toArray(Bit[]::new)));
	}

	@Test // TODO: Move to big word test
	default void canShiftByAtLeastOneWordOfBitsToTheRight() {
		Assumptions.assumeTrue(subject().length() > 70);
		assertThat(subject().rightShift(70), contains(stream(skip(subject(), 70)).toArray(Bit[]::new)));
	}

	@Test // TODO: Move to big word test
	default void canShiftByMultipleWordOfBitsToTheRight() {
		Assumptions.assumeTrue(subject().length() > 180);
		assertThat(subject().rightShift(180), contains(stream(skip(subject(), 180)).toArray(Bit[]::new)));
	}

	@Test
	default void isNotEmptyWhenLengthIsNonZero() {
		Assumptions.assumeTrue(subject().length() > 0);
		assertFalse(subject().isEmpty());
	}

	@Test
	default void isEmptyWhenLengthIsZero() {
		Assumptions.assumeTrue(subject().length() == 0);
		assertTrue(subject().isEmpty());
	}

	static Matcher<Iterable<Bit>> hasNoneOfTheBitsSet(Iterable<Bit> unsetBits) {
		return new TypeSafeMatcher<Iterable<Bit>>() {
			@Override
			protected boolean matchesSafely(Iterable<Bit> item) {
				return Streams.zip(stream(item), stream(unsetBits), Pair::of).noneMatch(it -> {
					return it.getLeft().equals(it.getRight()) && it.getLeft().isSet();
				});
			}

			@Override
			public void describeTo(Description description) {

			}
		};
	}

	@Test
	default void throwsExceptionOnIntersectsAgainstNull() {
		assertThrows(NullPointerException.class, () -> subject().intersects(null));
	}

	@Test
	default void alwaysReturnsTrueOnIntersectsWithSelf__AssumingNotEmptyBits() {
		Assumptions.assumeFalse(subject().isEmpty());
		assertTrue(subject().intersects(subject()));
	}

	@Test
	default void alwaysReturnsFalseOnIntersectsWithEmptyBits() {
		assertFalse(subject().intersects(empty()));
	}

	@Test
	default void returnsTrueOnIntersectsWithOverlappingBits() {
		assertTrue(subject().intersects(setBits()));
	}

	@Test
	default void returnsFalseOnIntersectsWithNonOverlappingBits() {
		assertFalse(subject().intersects(unsetBits()));
	}

	@Test
	default void throwsExceptionOnContainsAllAgainstNull() {
		assertThrows(NullPointerException.class, () -> subject().containsAll(null));
	}

	@Test
	default void alwaysReturnsTrueOnContainsAllWithSelf__AssumingNotEmptyBits() {
		Assumptions.assumeFalse(subject().isEmpty());
		assertTrue(subject().containsAll(subject()));
	}

	@Test
	default void alwaysReturnsTrueOnContainsAllWithEmptyBits() {
		assertTrue(subject().containsAll(empty()));
	}

	@Test
	default void returnsFalseOnContainsAllWithNonOverlappingBits() {
		assertFalse(subject().containsAll(unsetBits()));
	}

	@Test
	default void returnsFalseOnContainsAllWithPartiallyOverlappingBits() {
		assertFalse(subject().containsAll(setBits()));
	}
}
