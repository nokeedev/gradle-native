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

import dev.nokee.internal.testing.Assumptions;
import dev.nokee.model.internal.core.Bits;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.Bits.empty;
import static dev.nokee.model.internal.core.Bits.ofBits;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitsEmptyTest implements BitsTester {
	@Override
	public Bits subject() {
		return empty();
	}

	@Override
	public Bits unsetBits() {
		return ofBits(0xB0B);
	}

	@Override
	public Bits setBits() {
		return Assumptions.skipCurrentTestExecution("no bits is set on empty bits");
	}

	//	@Test
//	void canCreateEmptyBitsUsingZeroWord() {
//		assertEquals(empty(), Bits.ofBits(0));
//	}

	@Test
	void alwaysEmpty() {
		assertTrue(empty().isEmpty());
	}

	@Test
	void alwaysHasZeroLength() {
		assertEquals(0, empty().length());
	}

//	@Test
//	void alwaysReturnsEmptyBitsOnAndOperation() {
//		assertAll(
//			() -> assertEquals(empty(), empty().and(empty()), "when other is empty bits"),
//			() -> assertEquals(empty(), empty().or(nthBit(7)), "when other is single bit bits"),
//			() -> assertEquals(empty(), empty().or(ofBits(0b10110)), "when other is single word bits")
//		);
//	}

//	@Test
//	void alwaysReturnsOtherBitsOnOrOperation() {
//		assertAll(
//			() -> assertEquals(empty(), empty().or(empty()), "when other is empty bits"),
//			() -> assertEquals(nthBit(9), empty().or(nthBit(9)), "when other is single bit bits"),
//			() -> assertEquals(ofBits(0b101), empty().or(ofBits(0b101)), "when other is single word bits")
//		);
//	}
//
//	@Test
//	void alwaysReturnsEmptyBitsOnRightShiftOperation() {
//		assertAll(
//			() -> assertEquals(empty(), empty().rightShift(0), "when shifting by zero bit"),
//			() -> assertEquals(empty(), empty().rightShift(16), "when shifting by number of bit in short"),
//			() -> assertEquals(empty(), empty().rightShift(32), "when shifting by number of bit in integer"),
//			() -> assertEquals(empty(), empty().rightShift(64), "when shifting by number of bit in long"),
//			() -> assertEquals(empty(), empty().rightShift(128), "when shifting by number of bit in long long"),
//			() -> assertEquals(empty(), empty().rightShift(MAX_VALUE), "when shifting by unreasonable number of bits")
//		);
//	}
//
//	@Test
//	void throwsExceptionOnRightShiftOfNegativeValue() {
//		val ex = assertThrows(IllegalArgumentException.class, () -> empty().rightShift(-42));
//		assertEquals("Bit shift 'count' (-42) must be positive.", ex.getMessage());
//	}

//	@Test
//	void throwsExceptionWhenOtherBitsIsNullDuringAndOperation() {
//		assertThrows(NullPointerException.class, () -> empty().and(null));
//	}
//
//	@Test
//	void throwsExceptionWhenOtherBitsIsNullDuringOrOperation() {
//		assertThrows(NullPointerException.class, () -> empty().or(null));
//	}

	@Test
	void returnsMeaningfulToStringRepresentation() {
		assertEquals("empty bits", empty().toString());
	}

	@Test
	void hasNoBits() {
		assertIterableEquals(emptyList(), empty());
	}
}
