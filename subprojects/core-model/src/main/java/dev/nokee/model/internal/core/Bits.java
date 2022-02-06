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
package dev.nokee.model.internal.core;

import com.google.common.base.Preconditions;
import lombok.val;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.Integer.toUnsignedLong;
import static java.lang.System.arraycopy;

/**
 * Represent a compact vector of bits.
 */
public final class Bits implements Iterable<Bit> {
	private static final Bits EMPTY_BITS = new Bits();
	private static final int bitsPerSegment = 64;

	private final long[] bits;

	private Bits() {
		this.bits = new long[0];
	}

	private Bits(long bits) {
		this.bits = new long[] { bits };
	}

	private Bits(long[] bits) {
		this.bits = bits;
	}

	/**
	 * Returns true if this bit set contains no bits that are set to {@literal true}.
	 *
	 * @return {@code true} if the bit set has no bits set or {@code false} otherwise
	 */
	public boolean isEmpty() {
		return bits.length == 0;
	}

	/**
	 * Returns the result of a logical <b>AND</b> between this bit set and the specified bit set.
	 * This bit set is <b>not</b> modified and returns the result as a new value.
	 * A bit in the result has the value true if and only if the corresponding bit in both operand bit set has the value true.
	 *
	 * @param other  the other bit set operand, must not be null
	 * @return a new {@literal Bits} representing the result of the logical AND operation, never null
	 */
	public Bits and(Bits other) {
		Objects.requireNonNull(other);
		int commonWords = Math.min(bits.length, other.bits.length);
		long[] newBits = new long[commonWords];
		for (int i = 0; commonWords > i; i++) {
			newBits[i] = bits[i] & other.bits[i];
		}

		return ofBits(newBits);
	}

	/**
	 * Returns the result of a logical <b>OR</b> between this bit set and the specified bit set.
	 * This bit set is <b>not</b> modified and returns the result as a new value.
	 * A bit in the result has the value true if and only if either corresponding bit in operand's bit set has the value true.
	 *
	 * @param other  the other bit set operand, must not be null
	 * @return a new {@literal Bits} representing the result of the bitwise or operation, never null
	 */
	public Bits or(Bits other) {
		Objects.requireNonNull(other);
		long[] bits = this.bits;
		long[] otherBits = other.bits;
		int otherBitsLength = otherBits.length;
		int bitsLength = bits.length;
		int newBitsLength = Math.max(bits.length, other.bits.length);
		int commonWords = Math.min(bitsLength, otherBitsLength);
		long[] newBits = new long[newBitsLength];
		for (int i = 0; commonWords > i; i++) {
			newBits[i] = bits[i] | other.bits[i];
		}

		arraycopy(commonWords < otherBitsLength ? otherBits : bits, commonWords,
			newBits, commonWords, newBitsLength - commonWords);

		return ofBits(newBits);
	}

	/**
	 * Returns the result of a logical unsigned bit shift to the right of the specified count.
	 *
	 * @param count  the number of bits to shift to the right, must not be negative
	 * @return a new {@literal Bits} representing the result of the bit shift operation, never null
	 */
	public Bits rightShift(int count) {
		Preconditions.checkArgument(count >= 0, "Bit shift 'count' (%s) must be positive.", count);

		// Not shifting required
		if (count == 0) {
			return this;
		}

		// Shortcut on one word bitset
		if (bits.length == 1) {
			return ofBits(bits[0] >>> count);
		}

		int length = this.length();

		// Shifting more bit than available
		if (length <= count) {
			return empty();
		}

		int newCount = (length - count) / bitsPerSegment + ((length - count) % bitsPerSegment == 0 ? 0 : 1);
		long[] newBits = new long[newCount];

		// the word difference between source and destination shift, i.e. when shifting more than bitsPerSegment
		int wordShiftCount = count / bitsPerSegment;

		// the number of bits to shift within a word
		int bitShiftCount = count % bitsPerSegment;

		// the left shift required to keep bits between words, e.g. low bits becoming previous word's high bits
		int maskBitShiftCount = bitsPerSegment - bitShiftCount;

		for (int word = bits.length - 1; word >= 0 & word >= wordShiftCount; --word) {
			int i = word - wordShiftCount;

			// when high bits are kept
			if (i < newBits.length) {
				newBits[i] |= bits[word] >>> bitShiftCount;
			}
			if (i > 0) {
				newBits[i - 1] = bits[word] << maskBitShiftCount;
			}
		}

		return ofBits(newBits);
	}

	/**
	 * Returns the <i>logical size</i> of this bitset, e.g. the index of the highest set bit in the bitset plus one.
	 * If the bit set has no set bits, length will be zero.
	 *
	 * @return returns the logical size of this bitset, never negative
	 */
	public int length() {
		long[] bits = this.bits;
		for (int word = bits.length - 1; word >= 0; --word) {
			long bitsAtWord = bits[word];
			if (bitsAtWord != 0) {
				for (int bit = 63; bit >= 0; --bit) {
					if ((bitsAtWord & (1L << (bit & 0x3F))) != 0L) {
						return (word << 6) + bit + 1;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Returns true if the specified bit set has any bits set to true that are also set to true in the specified bit set.
	 *
	 * @param other  a bit set
	 * @return boolean indicating whether this bit set intersects the specified bit set
	 */
	public boolean intersects(Bits other) {
		Objects.requireNonNull(other);
		long[] bits = this.bits;
		long[] otherBits = other.bits;
		for (int i = Math.min(bits.length, otherBits.length) - 1; i >= 0; i--) {
			if ((bits[i] & otherBits[i]) != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this bit set is a super set of the specified set,
	 * i.e. it has all bits set to true that are also set to true in the specified BitSet.
	 *
	 * @param other  a bit set
	 * @return boolean indicating whether this bit set is a super set of the specified set
	 */
	public boolean containsAll(Bits other) {
		Objects.requireNonNull(other);
		long[] bits = this.bits;
		long[] otherBits = other.bits;
		int otherBitsLength = otherBits.length;
		int bitsLength = bits.length;

		for (int i = bitsLength; i < otherBitsLength; i++) {
			if (otherBits[i] != 0) {
				return false;
			}
		}
		for (int i = Math.min(bitsLength, otherBitsLength) - 1; i >= 0; i--) {
			if ((bits[i] & otherBits[i]) != otherBits[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (getClass() != o.getClass()) return false;

		Bits other = (Bits)o;
		long[] otherBits = other.bits;

		int commonWords = Math.min(bits.length, otherBits.length);
		for (int i = 0; commonWords > i; i++) {
			if (bits[i] != otherBits[i]) return false;
		}

		if (bits.length == otherBits.length) return true;

		return length() == other.length();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int word = length() / bitsPerSegment;
		int hash = 0;
		for (int i = 0; word >= i && word != 0; i++) {
			hash = 127 * hash + (int)(bits[i] ^ (bits[i] >>> 32));
		}
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Bit> iterator() {
		return new BitIterator(bits, length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (isEmpty()) {
			return "empty bits";
		} else {
			val builder = new StringBuilder();
			builder.append("bits '");
			copyOf(this).reverse().forEach(builder::append);
			builder.append("'");
			return builder.toString();
		}
	}

	/**
	 * Creates bitset where no bits are set.
	 *
	 * @return a unset bitset, never null
	 */
	public static Bits empty() {
		return EMPTY_BITS;
	}

	/**
	 * Creates bitset for the specified unsigned integer word.
	 *
	 * @param word  the unsigned integer bitset
	 * @return a new bitset for the specified unsigned integer word, never null
	 */
	// For automatic unsigned long conversion
	public static Bits ofBits(int word) {
		return new Bits(toUnsignedLong(word));
	}

	/**
	 * Creates bitset for the specified unsigned long word.
	 *
	 * @param word  the unsigned long bitset
	 * @return a new bitset for the specified unsigned long word, never null
	 */
	public static Bits ofBits(long word) {
		if (word == 0) {
			return EMPTY_BITS;
		} else {
			return new Bits(word);
		}
	}

	// For internal use only, removes unset high words
	private static Bits ofBits(long[] words) {
		int i = words.length - 1;
		while (i >= 0 && words[i] == 0) {
			--i;
		}
		if (i < 0) {
			return EMPTY_BITS;
		} else {
			return new Bits(Arrays.copyOfRange(words, 0, i + 1));
		}
	}

	/**
	 * Creates bitset where the specified nth bit is set.
	 *
	 * @param index  the bit index to set, must be positive
	 * @return a new bitset where the nth bit is set, never null
	 */
	public static Bits nthBit(int index) {
		Preconditions.checkArgument(index >= 0, "Bit 'index' (%s) must be positive.", index);
		int indexInLastSegment = index % bitsPerSegment;
		int count = index / bitsPerSegment + 1; // an additional partial segment is required
		long[] bitset = new long[count];
		bitset[count - 1] = (1L << indexInLastSegment);
		return new Bits(bitset);
	}

	// For automatic unsigned long conversion
	public static Bits ofBits(int mostSignificant, int to, int leastSignificant, int... others) {
		if (mostSignificant == 0) {
			if (others.length > 0) {
				return ofBits(to, leastSignificant, others[0], Arrays.copyOfRange(others, 1, others.length));
			} else {
				return ofBits(toUnsignedLong(leastSignificant) | (toUnsignedLong(to) << 32));
			}
		} else {
			// count number of whole long words
			int intWordCount = 3 + others.length; // first, count integer words
			int count = intWordCount / 2 + (intWordCount % 2 == 0 ? 0 : 1); // then, round up

			// allocate bitset
			long[] bitset = new long[count];
			int offset = 0;
			int i = others.length;
			while (offset < count) {
				if (i >= 2) {
					bitset[offset++] = toUnsignedLong(others[i--]) | (toUnsignedLong(others[i--]) << 32);
				} else if (i == 1) {
					bitset[offset++] = toUnsignedLong(others[i--]) | (toUnsignedLong(leastSignificant) << 32);
					bitset[offset++] = toUnsignedLong(to) | (toUnsignedLong(mostSignificant) << 32);
				} else if (i == 0) {
					bitset[offset++] = toUnsignedLong(leastSignificant) | (toUnsignedLong(to) << 32);
					bitset[offset++] = toUnsignedLong(mostSignificant);
				}
			}
			return new Bits(bitset);
		}
	}

	public static Bits ofBits(long mostSignificant, long toLeastSignificant, long... others) {
		if (mostSignificant == 0) {
			if (others.length > 0) {
				return ofBits(toLeastSignificant, others[0], Arrays.copyOfRange(others, 1, others.length));
			} else {
				return ofBits(toLeastSignificant);
			}
		} else {
			int count = 2 + others.length;
			long[] bitset = new long[count];
			int offset = 0;
			for (int i = others.length; i > 0;) {
				bitset[offset++] = others[--i];
			}
			bitset[offset++] = toLeastSignificant;
			bitset[offset] = mostSignificant;

			return new Bits(bitset);
		}
	}

	private static final class BitIterator implements Iterator<Bit> {
		private final long[] bits;
		private final int bitCount;
		private int currentCount = 0;

		private BitIterator(long[] bits, int bitCount) {
			this.bits = bits;
			this.bitCount = bitCount;
		}

		@Override
		public boolean hasNext() {
			return currentCount < bitCount;
		}

		@Override
		public Bit next() {
			return nextBit(currentCount++);
		}

		private Bit nextBit(int bitCount) {
			int word = bitCount / bitsPerSegment;
			int bitIndex = bitCount % bitsPerSegment;
			if ((bits[word] & (1L << bitIndex)) == 0) {
				return Bit.Zero;
			} else {
				return Bit.One;
			}
		}
	}
}
