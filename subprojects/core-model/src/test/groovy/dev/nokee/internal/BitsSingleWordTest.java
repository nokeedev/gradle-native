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

import static dev.nokee.model.internal.core.Bit.One;
import static dev.nokee.model.internal.core.Bit.Zero;
import static dev.nokee.model.internal.core.Bits.empty;
import static dev.nokee.model.internal.core.Bits.ofBits;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BitsSingleWordTest implements BitsTester {
	@Override
	public Bits subject() {
		return ofBits(0xC00010FF);
	}

	@Override
	public Bits unsetBits() {
		return ofBits(0xEF00L);
	}

	@Override
	public Bits setBits() {
		return ofBits(0x800030AA);
	}

	@Test
	void neverEmpty() {
		assertFalse(ofBits(0b1).isEmpty());
		assertFalse(ofBits(0b1011).isEmpty());
		assertFalse(ofBits(0b101101).isEmpty());
	}

	@Test
	void returnsLengthAsNumberOfBitsToStoreUpToTheMostSignificantBit() {
		assertAll(
			() -> assertEquals(1, ofBits(0b1).length()),
			() -> assertEquals(3, ofBits(0b111).length()),
			() -> assertEquals(8, ofBits(0x8F).length()),
			() -> assertEquals(16, ofBits(0x800F).length()),
			() -> assertEquals(32, ofBits(0x81250300).length()),
			() -> assertEquals(64, ofBits(0x8000E40030341983L).length())
		);
	}

//	@Test
//	void returnsSameBitsWhenOrOperationOnSameBits() {
//		val bits = ofBits(0b1);
//		assertSame(bits, bits.or(bits));
//	}

	@Test
	void returnsAllActivatedBitsFromBothBitsDuringOrOperation() {
		assertEquals(ofBits(0b111), ofBits(0b101).or(ofBits(0b11)));
		assertEquals(ofBits(0b111), ofBits(0b110).or(ofBits(0b11)));
		assertEquals(ofBits(0b11111011), ofBits(0b11111011).or(ofBits(0b1000000)));
	}

//	@Test
//	void returnsCurrentBitsOnOrOperationWithEmptyBits() {
//		assertEquals(ofBits(0b10101), ofBits(0b10101).or(empty()));
//	}
//
//	@Test
//	void returnsSameBitsWhenAndOperationOnSameBits() {
//		val bits = ofBits(0b1);
//		assertEquals(bits, bits.and(bits));
//	}

	@Test
	void returnsOnlyActivatedBitsFromBothBitsDuringAndOperation() {
		assertEquals(ofBits(0b1), ofBits(0b101).and(ofBits(0b11)));
		assertEquals(ofBits(0b10), ofBits(0b110).and(ofBits(0b11)));
		assertEquals(ofBits(0b01000000), ofBits(0b11111011).and(ofBits(0b1000000)));
	}

//	@Test
//	void returnsEmptyBitsOnAndOperationWithEmptyBits() {
//		assertEquals(empty(), ofBits(0b101010).and(empty()));
//	}

//	@Test
//	void throwsExceptionWhenOtherBitsIsNullDuringAndOperation() {
//		assertThrows(NullPointerException.class, () -> ofBits(0b1).and(null));
//	}
//
//	@Test
//	void throwsExceptionWhenOtherBitsIsNullDuringOrOperation() {
//		assertThrows(NullPointerException.class, () -> ofBits(0b1).or(null));
//	}
//
//	@Test
//	void returnsEmptyBitsWhenRightShiftLeastSignificantBitOutOfBits() {
//		assertEquals(empty(), ofBits(0b1).rightShift(1));
//	}
//
//	@Test
//	void returnsEmptyBitsWhenRightShiftLeastSignificantBitsOutOfBits() {
//		assertEquals(empty(), ofBits(0b1010).rightShift(4));
//	}
//
//	@Test
//	void returnsEmptyBitsWhenRightShiftMoreBitsThanAvailable() {
//		assertEquals(empty(), ofBits(0b1010).rightShift(42));
//	}

	@Test
	void alwaysShiftBitsAsUnsignedByteBits() {
		assertEquals(ofBits(0x08), ofBits(0x80).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedShortBits() {
		assertEquals(ofBits(0x0800), ofBits(0x8000).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedIntegerBits() {
		assertEquals(ofBits(0x08000000), ofBits(0x80000000).rightShift(4));
	}

	@Test
	void alwaysShiftBitsAsUnsignedLongBits() {
		assertEquals(ofBits(0x0800000000000000L), ofBits(0x8000000000000000L).rightShift(4));
	}

	@Test
	void returnsMeaningfulToStringRepresentation() {
		assertEquals("bits '10001001'", ofBits(0b10001001).toString());
	}

	@Test
	void hasSingleBitOnOneBit() {
		assertIterableEquals(singletonList(One), ofBits(0b1));
	}

	@Test
	void iterateFromLeastSignificantBit() {
		assertIterableEquals(asList(One, One, One, Zero, Zero, One), ofBits(0b100111));
	}

	@Test
	void stopsIterationOnMostSignificantBit() {
		assertIterableEquals(asList(One, Zero, One), ofBits(0b000101));
	}
}
