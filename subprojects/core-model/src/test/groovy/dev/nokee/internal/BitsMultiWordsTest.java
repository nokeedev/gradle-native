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
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.Bits.ofBits;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BitsMultiWordsTest implements BitsTester {
	@Override
	public Bits subject() {
		return ofBits(0xCAFEL, 0xD00DL);
	}

	@Override
	public Bits unsetBits() {
		return ofBits(0x501L, 0x0FF0L);
	}

	@Override
	public Bits setBits() {
		return ofBits(0xC1FEL, 0xD010L);
	}

	@Test
	void neverEmpty() {
		assertFalse(ofBits(0b1, 0b101, 0b11).isEmpty());
		assertFalse(ofBits(0b11, 0, 0).isEmpty());
		assertFalse(ofBits(0b10110L, 0b0111L).isEmpty());
		assertFalse(ofBits(0b10110L, 0L).isEmpty());
	}

	@Test
	void returnsPositionOfMostSignificantSetBit() {
		assertAll(
			() -> assertEquals(65, ofBits(0b1L, 0x42).length()),
			() -> assertEquals(65, ofBits(0b1, 0x4, 0x2).length()),
			() -> assertEquals(75, ofBits(0b10000110101L, 0x52).length()),
			() -> assertEquals(129, ofBits(0b1L, 0, 0).length()),
			() -> assertEquals(194, ofBits(0b10L, 0, 0, 0).length())
		);
	}
}
