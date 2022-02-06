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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.Bits.empty;
import static dev.nokee.model.internal.core.Bits.nthBit;
import static dev.nokee.model.internal.core.Bits.ofBits;

class BitsEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquality() {
		new EqualsTester()
			// No bit set
			.addEqualityGroup(empty(), ofBits(0), ofBits(0L),
				ofBits(0, 0, 0),
				ofBits(0, 0, 0, 0, 0),
				ofBits(0L, 0L),
				ofBits(0L, 0L, 0L)
			)

			// Only least significant bit set
			.addEqualityGroup(nthBit(0), ofBits(0b1),
				ofBits(0, 0, 0b1),
				ofBits(0L, 0b1L)
			)

			// Only most significant bit set of first word
			.addEqualityGroup(nthBit(63), ofBits(0x8000000000000000L),
				ofBits(0L, 0x8000000000000000L)
			)

			// Only most significant bit set within byte word
			.addEqualityGroup(nthBit(3), ofBits(0b1000), ofBits(0b1000L),
				ofBits(0, 0, 0b1000),
				ofBits(0L, 0b1000L)
			)

			// Only most significant bit set within short word
			.addEqualityGroup(nthBit(10), ofBits(0b10000000000), ofBits(0b10000000000L),
				ofBits(0, 0, 0b10000000000),
				ofBits(0L, 0b10000000000L)
			)

			// Only most significant bit set within integer word
			.addEqualityGroup(nthBit(24), ofBits(0x1000000), ofBits(0x1000000L),
				ofBits(0, 0, 0x1000000),
				ofBits(0L, 0x1000000L)
			)

			// Only most significant bit set within long word
			.addEqualityGroup(nthBit(52), ofBits(0x10000000000000L),
				ofBits(0, 0, 0x100000, 0),
				ofBits(0L, 0x10000000000000L)
			)

			// Only most significant bit set within long long word
			.addEqualityGroup(nthBit(100),
				ofBits(0b10000, 0, 0, 0),
				ofBits(0x1000000000L, 0L)
			)

			// Bits in single words
			.addEqualityGroup(ofBits(0xBABE), ofBits(0xBABEL))

			// Bits in multiple words
			.addEqualityGroup(
				ofBits(0xC0FFEEL, 0xBABEL),
				ofBits(0xC0FFEE, 0, 0xBABE)
			)
			.testEquals();
	}
}
