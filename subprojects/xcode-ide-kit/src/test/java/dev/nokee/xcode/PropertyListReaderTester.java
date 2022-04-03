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
package dev.nokee.xcode;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_START;
import static dev.nokee.xcode.PropertyListReader.Event.BOOLEAN;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_END;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_KEY;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_END;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_START;
import static dev.nokee.xcode.PropertyListReader.Event.INTEGER;
import static dev.nokee.xcode.PropertyListReader.Event.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

abstract class PropertyListReaderTester {
	@Nested
	class BooleanTest {
		@Test
		void canReadSingleTrueBoolean() {
			val subject = newSingleTrueBooleanReader();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(booleanType()));
			assertThat(subject.readBoolean(), equalTo(true));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleFalseBoolean() {
			val subject = newSingleFalseBooleanReader();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(booleanType()));
			assertThat(subject.readBoolean(), equalTo(false));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	PropertyListReader.Event booleanType() {
		return BOOLEAN;
	}

	abstract PropertyListReader newSingleTrueBooleanReader();
	abstract PropertyListReader newSingleFalseBooleanReader();

	@Nested
	class IntegerTest {
		@Test
		void canReadSingleUInt8() {
			val subject = newSingleUInt8Reader_Hex42();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x42L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleUInt16() {
			val subject = newSingleUInt16Reader_Hex4241();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x4241L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleUInt32() {
			val subject = newSingleUInt32Reader_Hex4241999();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x4241999L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	PropertyListReader.Event integerType() {
		return INTEGER;
	}

	abstract PropertyListReader newSingleUInt8Reader_Hex42();
	abstract PropertyListReader newSingleUInt16Reader_Hex4241();
	abstract PropertyListReader newSingleUInt32Reader_Hex4241999();

	@Nested
	class StringTest {
		@Test
		void canReadSingleAlphanumericStringWithoutSpaces() {
			val subject = newSingleAlphanumericString_alpha456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("alpha456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleAlphanumericStringWithSpaces() {
			val subject = newSingleAlphanumericStringWithSpaces_alpha_space_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("alpha 456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newSingleAlphanumericString_alpha456();
	abstract PropertyListReader newSingleAlphanumericStringWithSpaces_alpha_space_456();

	@Nested
	class ArrayTest {
		@Test
		void canReadEmptyArray() {
			val subject = newSingleArray_empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleArrayWithInteger() {
			val subject = newSingleArray_hex52();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x52L));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleArrayWithStringAndIntegerElement() {
			val subject = newSingleArray__myString_hex98();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("myString"));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x98L));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newSingleArray_empty();
	abstract PropertyListReader newSingleArray_hex52();
	abstract PropertyListReader newSingleArray__myString_hex98();

	@Nested
	class DictTest {
		@Test
		void canReadEmptyDict() {
			val subject = newSingleDict_empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadSingleDictWithInteger() {
			val subject = newSingleDict__myKey_to_hex78();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("myKey"));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(0x78L));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newSingleDict_empty();
	abstract PropertyListReader newSingleDict__myKey_to_hex78();
}
