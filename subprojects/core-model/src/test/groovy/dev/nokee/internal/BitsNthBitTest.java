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

import dev.nokee.model.internal.core.Bits;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Iterables.limit;
import static dev.nokee.model.internal.core.Bit.One;
import static dev.nokee.model.internal.core.Bit.Zero;
import static dev.nokee.model.internal.core.Bits.nthBit;
import static dev.nokee.model.internal.core.Bits.ofBits;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitsNthBitTest implements BitsTester {
	@Override
	public Bits subject() {
		return nthBit(42);
	}

	@Override
	public Bits unsetBits() {
		return nthBit(32);
	}

	@Override
	public Bits setBits() {
		return ofBits(0x40000C00000L);
	}

	@Test
	void alwaysShiftBitsAsUnsignedByteBits() {
		assertEquals(ofBits(0x08), nthBit(7).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedShortBits() {
		assertEquals(ofBits(0x0800), nthBit(15).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedIntegerBits() {
		assertEquals(ofBits(0x08000000), nthBit(31).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedLongBits() {
		assertEquals(ofBits(0x0800000000000000L), nthBit(63).rightShift(4));
	}

	@Test
	void throwsExceptionIfBitIndexIsNegative() {
		val ex = assertThrows(IllegalArgumentException.class, () -> nthBit(-42));
		assertEquals("Bit 'index' (-42) must be positive.", ex.getMessage());
	}

	@Test
	void returnsLengthInNumberOfBits() {
		assertAll(
			() -> assertEquals(1, nthBit(0).length()),
			() -> assertEquals(33, nthBit(32).length()),
			() -> assertEquals(64, nthBit(63).length()),
			() -> assertEquals(1000, nthBit(999).length())
		);
	}

	@Test
	void canIterateSingleBitIndex() {
		assertThat(nthBit(3), contains(Zero, Zero, Zero, One));
	}

	@Test
	void hasAlwaysZeroInAllOtherPositionDuringIteration() {
		assertThat(limit(nthBit(5), 4), everyItem(equalTo(Zero)));
		assertThat(limit(nthBit(32), 31), everyItem(equalTo(Zero)));
		assertThat(limit(nthBit(54), 53), everyItem(equalTo(Zero)));
		assertThat(limit(nthBit(100), 99), everyItem(equalTo(Zero)));
	}

	@Test
	void neverEmpty() {
		assertFalse(nthBit(0).isEmpty());
		assertFalse(nthBit(1).isEmpty());
		assertFalse(nthBit(10).isEmpty());
	}

	@Test
	void returnsMeaningfulToStringRepresentation() {
		assertEquals("bits '10000000'", nthBit(7).toString());
	}
}
