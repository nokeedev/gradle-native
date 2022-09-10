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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

abstract class PropertyListWriterTester {
	abstract PropertyListWriter subject();

	@Nested
	class DocumentTest {
		@Test
		void canWriteEmptyDocument() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeEndDocument();

			verifyDocument__empty();
		}
	}

	abstract void verifyDocument__empty();

	@Nested
	class BooleanTest {
		@Test
		void canWriteDocumentWithSingleTrueBoolean() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeBoolean(true);
			subject().writeEndDocument();

			verifyDocumentWithBoolean__true();
		}

		@Test
		void canWriteDocumentWithSingleFalseBoolean() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeBoolean(false);
			subject().writeEndDocument();

			verifyDocumentWithBoolean__false();
		}
	}

	abstract void verifyDocumentWithBoolean__true();
	abstract void verifyDocumentWithBoolean__false();

	@Nested
	class IntegerTest {
		@Test
		void canWriteDocumentWithSingleInt8() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeInteger(34);
			subject().writeEndDocument();

			verifyDocumentWithInteger__34();
		}

		@Test
		void canWriteDocumentWithSingleInt16() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeInteger(9216);
			subject().writeEndDocument();

			verifyDocumentWithInteger__9216();
		}

		@Test
		void canWriteDocumentWithSingleInt32() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeInteger(541069328);
			subject().writeEndDocument();

			verifyDocumentWithInteger__541069328();
		}

		@Test
		void canWriteDocumentWithSingleInt64() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeInteger(2306142076443623952L);
			subject().writeEndDocument();

			verifyDocumentWithInteger__2306142076443623952();
		}
	}

	abstract void verifyDocumentWithInteger__34();
	abstract void verifyDocumentWithInteger__9216();
	abstract void verifyDocumentWithInteger__541069328();
	abstract void verifyDocumentWithInteger__2306142076443623952();

	@Nested
	class RealTest {
		@Test
		void canWriteDocumentWithSingleReal() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeReal(4.2f);
			subject().writeEndDocument();

			verifyDocumentWithReal__4_2();
		}
	}

	abstract void verifyDocumentWithReal__4_2();

	@Nested
	class StringTest {
		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithoutSpaces() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha567();
		}

		@ParameterizedTest
		@ValueSource(chars = {'?', '!', '(', ')', '[', ']', '{', '}', '*'})
		void canWriteDocumentWithSingleSpecialCharacterString(char specialChar) {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha" + specialChar + "567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_special_567(specialChar);
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithForwardSlashCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha/567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_slash_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithDotCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha.567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_dot_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithUnderscoreCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha_567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_underscore_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithDollarSignCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha$567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_dollarSign_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithColonCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha:567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_colon_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithDashCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha-567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_dash_567();
		}

		@Test
		void canWriteDocumentWithSingleAlphanumericStringWithSpace() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha 567");
			subject().writeEndDocument();

			verifyDocumentWithString__alpha_space_567();
		}

		@Test
		void canWriteDocumentWithSingleEmptyString() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("");
			subject().writeEndDocument();

			verifyDocumentWithString__empty();
		}
	}

	abstract void verifyDocumentWithString__alpha567();
	abstract void verifyDocumentWithString__alpha_special_567(char special);
	abstract void verifyDocumentWithString__alpha_slash_567();
	abstract void verifyDocumentWithString__alpha_dot_567();
	abstract void verifyDocumentWithString__alpha_underscore_567();
	abstract void verifyDocumentWithString__alpha_dollarSign_567();
	abstract void verifyDocumentWithString__alpha_colon_567();
	abstract void verifyDocumentWithString__alpha_dash_567();
	abstract void verifyDocumentWithString__alpha_space_567();
	abstract void verifyDocumentWithString__empty();

	@Nested
	class ArrayTest {
		@Test
		void canWriteDocumentWithSingleEmptyArray() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeEmptyArray();
			subject().writeEndDocument();

			verifyDocumentWithArray__empty();
		}

		@Test
		void canWriteDocumentWithSingleArrayOfIntegerElement() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartArray(1);
			subject().writeInteger(17440);
			subject().writeEndArray();
			subject().writeEndDocument();

			verifyDocumentWithArray__17440();
		}

		@Test
		void canWriteDocumentWithSingleArrayOfIntegerAndStringElements() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartArray(1);
			subject().writeInteger(384);
			subject().writeString("aString");
			subject().writeEndArray();
			subject().writeEndDocument();

			verifyDocumentWithArray__384_aString();
		}

		@Test
		void canWriteDocumentWithSingleArrayOfArrayOfIntegers() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartArray(1);
			subject().writeStartArray(3);
			subject().writeInteger(0);
			subject().writeInteger(1);
			subject().writeInteger(2);
			subject().writeEndArray();
			subject().writeEndArray();
			subject().writeEndDocument();

			verifyDocumentWithArray__arrayOf_0_1_2();
		}
	}

	abstract void verifyDocumentWithArray__empty();
	abstract void verifyDocumentWithArray__17440();
	abstract void verifyDocumentWithArray__384_aString();
	abstract void verifyDocumentWithArray__arrayOf_0_1_2();

	@Nested
	class DictionaryTest {
		@Test
		void canWriteDocumentWithSingleEmptyDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeEmptyDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionary__empty();
		}

		@Test
		void canWriteDocumentWithSingleIntegerElementDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("aKey");
			subject().writeInteger(4608);
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionary__aKey_to_4608();
		}

		@Test
		void canWriteDocumentWithSingleStringElementDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("aKey");
			subject().writeString("myValue");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionary__aKey_to_myValue();
		}

		@Test
		void canWriteDocumentWithSingleDictionaryWithMultipleElements() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(3);
			subject().writeDictionaryKey("k0");
			subject().writeString("first");
			subject().writeDictionaryKey("k1");
			subject().writeInteger(2);
			subject().writeDictionaryKey("k2");
			subject().writeBoolean(false);
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionary__k0_to_first__k1_to_2__k2_to_false();
		}

		@Test
		void canWriteDocumentWithSingleDictionaryWithNestedDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("aKey");
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("myKey");
			subject().writeString("myValue");
			subject().writeEndDictionary();
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionary__aKey_to_dictOf_myKey_to_myValue();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithoutSpaces() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha567();
		}

		@ParameterizedTest
		@ValueSource(chars = {'?', '!', '(', ')', '[', ']', '{', '}', '*'})
		void canWriteDocumentWithDictionaryKeyOfSpecialCharacterString(char specialChar) {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha" + specialChar + "567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_special_567(specialChar);
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithForwardSlashCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha/567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_slash_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithDotCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha.567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_dot_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithUnderscoreCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha_567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_underscore_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithDollarSignCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha$567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_dollarSign_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithColonCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha:567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_colon_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithDashCharacter() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha-567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_dash_567();
		}

		@Test
		void canWriteDocumentWithDictionaryKeyOfAlphanumericStringWithSpace() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("alpha 567");
			subject().writeString("test");
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifyDocumentWithDictionaryKey__alpha_space_567();
		}
	}

	abstract void verifyDocumentWithDictionary__empty();
	abstract void verifyDocumentWithDictionary__aKey_to_4608();
	abstract void verifyDocumentWithDictionary__aKey_to_myValue();
	abstract void verifyDocumentWithDictionary__k0_to_first__k1_to_2__k2_to_false();
	abstract void verifyDocumentWithDictionary__aKey_to_dictOf_myKey_to_myValue();

	abstract void verifyDocumentWithDictionaryKey__alpha567();
	abstract void verifyDocumentWithDictionaryKey__alpha_special_567(char specialChar);
	abstract void verifyDocumentWithDictionaryKey__alpha_slash_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_dot_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_underscore_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_dollarSign_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_colon_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_dash_567();
	abstract void verifyDocumentWithDictionaryKey__alpha_space_567();

	@Nested
	class DateTest {
		@Test
		void writesDateAsFormattedInISO8601() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeDate(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
			subject().writeEndDocument();

			verifyDocumentWithDate__epoch();
		}
	}

	abstract void verifyDocumentWithDate__epoch();

	@Nested
	class DataTest {
		@Test
		void writesDataAsLowerCaseHexString() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeData(new byte[] { (byte) 0xB0, 0x0B});
			subject().writeEndDocument();

			verifyDocumentWithData__b00b();
		}
	}

	abstract void verifyDocumentWithData__b00b();
}
