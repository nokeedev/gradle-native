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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_START;
import static dev.nokee.xcode.PropertyListReader.Event.BOOLEAN;
import static dev.nokee.xcode.PropertyListReader.Event.DATA;
import static dev.nokee.xcode.PropertyListReader.Event.DATE;
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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class PropertyListReaderTester {
	@Nested
	class DocumentTest {
		@Test
		void canReadEmptyDocument() {
			val subject = newDocument__empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void throwsNoSuchElementExceptionAfterDocumentEndEvent() {
			val subject = newDocument__empty();
			subject.next(); // DOCUMENT_START
			subject.next(); // DOCUMENT_END
			assertThat(subject.hasNext(), equalTo(false));
			val ex = assertThrows(NoSuchElementException.class, () -> subject.next());
			assertThat(ex.getMessage(), equalTo("DOCUMENT_END reached: no more elements on the stream."));
		}
	}

	abstract PropertyListReader newDocument__empty();

	@Nested
	class BooleanTest {
		@Test
		void canReadDocumentWithSingleTrueBoolean() {
			val subject = newDocumentWithBoolean__true();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(booleanType()));
			assertThat(subject.readBoolean(), equalTo(true));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleFalseBoolean() {
			val subject = newDocumentWithBoolean__false();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(booleanType()));
			assertThat(subject.readBoolean(), equalTo(false));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	PropertyListReader.Event booleanType() {
		return BOOLEAN;
	}

	abstract PropertyListReader newDocumentWithBoolean__true();
	abstract PropertyListReader newDocumentWithBoolean__false();

	@Nested
	class IntegerTest {
		@Test
		void canReadDocumentWithSingleInt8() {
			val subject = newDocumentWithInteger__26();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(26L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleInt16() {
			val subject = newDocumentWithInteger__12612();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(12612L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleInt32() {
			val subject = newDocumentWithInteger__272760970();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(272760970L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleInt64() {
			val subject = newDocumentWithInteger__2380154602107442436();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(2380154602107442436L));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	PropertyListReader.Event integerType() {
		return INTEGER;
	}

	abstract PropertyListReader newDocumentWithInteger__26();
	abstract PropertyListReader newDocumentWithInteger__12612();
	abstract PropertyListReader newDocumentWithInteger__272760970();
	abstract PropertyListReader newDocumentWithInteger__2380154602107442436();

	@Nested
	class StringTest {
		@Test
		void canReadDocumentWithSingleAlphanumericStringWithoutSpaces() {
			val subject = newDocumentWithString__beta456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@ParameterizedTest
		@ValueSource(chars = {'?', '!', '(', ')', '[', ']', '{', '}', '*'})
		void canReadDocumentWithSingleSpecialCharacterString(char specialChar) {
			val subject = newDocumentWithString__beta_special_456(specialChar);
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta" + specialChar + "456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithForwardSlashCharacter() {
			val subject = newDocumentWithString__beta_slash_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta/456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithDotCharacter() {
			val subject = newDocumentWithString__beta_dot_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta.456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithUnderscoreCharacter() {
			val subject = newDocumentWithString__beta_underscore_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta_456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithDollarSignCharacter() {
			val subject = newDocumentWithString__beta_dollarSign_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta$456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithDashCharacter() {
			val subject = newDocumentWithString__beta_dash_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta-456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithColonCharacter() {
			val subject = newDocumentWithString__beta_colon_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta:456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleAlphanumericStringWithSpace() {
			val subject = newDocumentWithString__beta_space_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("beta 456"));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleEmptyString() {
			val subject = newDocumentWithString__empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo(""));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newDocumentWithString__beta456();
	abstract PropertyListReader newDocumentWithString__beta_special_456(char special);
	abstract PropertyListReader newDocumentWithString__beta_slash_456();
	abstract PropertyListReader newDocumentWithString__beta_dot_456();
	abstract PropertyListReader newDocumentWithString__beta_underscore_456();
	abstract PropertyListReader newDocumentWithString__beta_dollarSign_456();
	abstract PropertyListReader newDocumentWithString__beta_dash_456();
	abstract PropertyListReader newDocumentWithString__beta_colon_456();
	abstract PropertyListReader newDocumentWithString__beta_space_456();
	abstract PropertyListReader newDocumentWithString__empty();

	@Nested
	class ArrayTest {
		@Test
		void canReadDocumentWithSingleEmptyArray() {
			val subject = newDocumentWithArray__empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleArrayOfIntegerElement() {
			val subject = newDocumentWithArray__8706();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(8706L));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleArrayOfStringAndIntegerElements() {
			val subject = newDocumentWithArray__myString_9762();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("myString"));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(9762L));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canDocumentDocumentWithSingleArrayOfArrayOfIntegerElements() {
			val subject = newDocumentWithArray__arrayOf_4_5_6();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(ARRAY_START));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(4L));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(5L));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(6L));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(ARRAY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newDocumentWithArray__empty();
	abstract PropertyListReader newDocumentWithArray__8706();
	abstract PropertyListReader newDocumentWithArray__myString_9762();
	abstract PropertyListReader newDocumentWithArray__arrayOf_4_5_6();

	@Nested
	class DictTest {
		@Test
		void canReadDocumentWithSingleEmptyDictionary() {
			val subject = newDocumentWithDictionary__empty();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleDictionaryOfIntegerEntry() {
			val subject = newDocumentWithDictionary__myKey_to_2098176();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("myKey"));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(2098176L));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleDictionaryOfStringEntry() {
			val subject = newDocumentWithDictionary__myKey_to_aValue();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("myKey"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("aValue"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithSingleDictionaryOfMultipleElements() {
			val subject = newDocumentWithDictionary__k0_to_true__k1_to_second__k2_to_3();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("k0"));
			assertThat(subject.next(), is(booleanType()));
			assertThat(subject.readBoolean(), equalTo(true));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("k1"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("second"));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("k2"));
			assertThat(subject.next(), is(integerType()));
			assertThat(subject.readInteger(), equalTo(3L));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithoutSpaces() {
			val subject = newDocumentWithDictionaryKey__beta456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@ParameterizedTest
		@ValueSource(chars = {'?', '!', '(', ')', '[', ']', '{', '}', '*'})
		void canReadDocumentWithDictionaryKeyOfSpecialCharacterString(char specialChar) {
			val subject = newDocumentWithDictionaryKey__beta_special_456(specialChar);
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta" + specialChar + "456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithForwardSlashCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_slash_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta/456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithDotCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_dot_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta.456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithUnderscoreCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_underscore_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta_456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithDollarSignCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_dollarSign_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta$456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithDashCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_dash_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta-456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithColonCharacter() {
			val subject = newDocumentWithDictionaryKey__beta_colon_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta:456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}

		@Test
		void canReadDocumentWithDictionaryKeyOfAlphanumericStringWithSpace() {
			val subject = newDocumentWithDictionaryKey__beta_space_456();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DICTIONARY_START));
			assertThat(subject.next(), is(DICTIONARY_KEY));
			assertThat(subject.readDictionaryKey(), equalTo("beta 456"));
			assertThat(subject.next(), is(STRING));
			assertThat(subject.readString(), equalTo("test"));
			assertThat(subject.next(), is(DICTIONARY_END));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newDocumentWithDictionary__empty();
	abstract PropertyListReader newDocumentWithDictionary__myKey_to_2098176();
	abstract PropertyListReader newDocumentWithDictionary__myKey_to_aValue();
	abstract PropertyListReader newDocumentWithDictionary__k0_to_true__k1_to_second__k2_to_3();

	abstract PropertyListReader newDocumentWithDictionaryKey__beta456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_special_456(char specialChar);
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_slash_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_dot_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_underscore_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_dollarSign_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_dash_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_colon_456();
	abstract PropertyListReader newDocumentWithDictionaryKey__beta_space_456();

	@Nested
	class DateTest {
		@Test
		void canReadDocumentWithSingleEpochDate() {
			val subject = newDocumentWithDate__epoch();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(dateType()));
			assertThat(subject.readDate(), equalTo(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)));
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	PropertyListReader.Event dateType() {
		return DATE;
	}

	abstract PropertyListReader newDocumentWithDate__epoch();

	@Nested
	class DataTest {
		@Test
		void canReadDocumentWithSingleData() {
			val subject = newDocumentWithData__c0ffee();
			assertThat(subject.next(), is(DOCUMENT_START));
			assertThat(subject.next(), is(DATA));
			assertArrayEquals(new byte[] { (byte) 0xc, (byte) 0x0, (byte) 0xf, (byte) 0xf, (byte) 0xe, (byte) 0xe }, subject.readData());
			assertThat(subject.next(), is(DOCUMENT_END));
		}
	}

	abstract PropertyListReader newDocumentWithData__c0ffee();
}
