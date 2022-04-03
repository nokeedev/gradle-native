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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

abstract class PropertyListWriterTester {
	abstract PropertyListWriter subject();

	@Nested
	class BooleanTest {
		@Test
		void writesTrueBooleanValue() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeBoolean(true);
			subject().writeEndDocument();

			verifySingleTrueValue();
		}

		@Test
		void writesFalseBooleanValue() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeBoolean(false);
			subject().writeEndDocument();

			verifySingleFalseValue();
		}
	}

	abstract void verifySingleTrueValue();
	abstract void verifySingleFalseValue();

	@Nested
	class IntegerTest {
		@Test
		void writesIntegerValue() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeInteger(42);
			subject().writeEndDocument();

			verifySingleIntegerValue(42);
		}
	}

	abstract void verifySingleIntegerValue(int expected);

	@Nested
	class RealTest {
		@Test
		void writesRealValue() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeReal(4.2f);
			subject().writeEndDocument();

			verifySingleRealValue(4.2f);
		}
	}

	abstract void verifySingleRealValue(float expected);

	@Nested
	class StringTest {
		@Test
		void writesAlphanumericWithoutSpaceString() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha567");
			subject().writeEndDocument();

			verifyAlphanumericWithoutSpaceString("alpha567");
		}

		@ParameterizedTest
		@ValueSource(chars = {'.', '-', '_', '?', '!', '(', ')'})
		void writesStringContainingNonAlphanumericCharacter(char specialChar) {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha" + specialChar + "567");
			subject().writeEndDocument();

			verifyNonAlphanumericWithoutSpaceString("alpha" + specialChar + "567");
		}

		@Test
		void writesAlphanumericStringContainingSpace() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeString("alpha 567");
			subject().writeEndDocument();

			verifyNonAlphanumericWithoutSpaceString("alpha 567");
		}
	}

	abstract void verifyAlphanumericWithoutSpaceString(String expected);

	abstract void verifyNonAlphanumericWithoutSpaceString(String expected);

	@Nested
	class DictionaryTest {
		@Test
		void writesEmptyDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeEmptyDictionary();
			subject().writeEndDocument();

			verifyEmptyDictionary();
		}

		@Test
		void writesSingleIntegerElementDictionary() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartDictionary(1);
			subject().writeDictionaryKey("myKey");
			subject().writeInteger(42);
			subject().writeEndDictionary();
			subject().writeEndDocument();

			verifySingleIntegerElementDictionary(ImmutableMap.of("myKey", 42));
		}
	}

	abstract void verifyEmptyDictionary();

	abstract void verifySingleIntegerElementDictionary(Map<String, Object> expected);

	@Nested
	class ArrayTest {
		@Test
		void writesEmptyArray() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeEmptyArray();
			subject().writeEndDocument();

			verifyEmptyArray();
		}

		@Test
		void writesSingleIntegerElementArray() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeStartArray(1);
			subject().writeInteger(42);
			subject().writeEndArray();
			subject().writeEndDocument();

			verifyArray(42);
		}
	}

	abstract void verifyEmptyArray();

	abstract void verifyArray(Object... expectedElements);

	@Nested
	class DateTest {
		@Test
		void writesDateAsFormattedInISO8601() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeDate(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
			subject().writeEndDocument();

			verifyEpochDate();
		}
	}

	abstract void verifyEpochDate();

	@Nested
	class DataTest {
		@Test
		void writesDataAsLowerCaseHexString() {
			subject().writeStartDocument(PropertyListVersion.VERSION_00);
			subject().writeData(new byte[] { (byte) 0xB0, 0x0B});
			subject().writeEndDocument();

			verifyData_BOOB();
		}
	}

	abstract void verifyData_BOOB();
}
