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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AsciiPropertyListWriterTest extends PropertyListWriterTester {
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private final AsciiPropertyListWriter subject = new AsciiPropertyListWriter(new OutputStreamWriter(outStream));

	private String output() {
		return outStream.toString().replaceAll("\n\r?", "\n");
	}

	@Test
	void writesUTF8HeaderOnDocumentStart() {
		subject.writeStartDocument();
		subject.writeEndDocument();

		assertThat(output(), equalTo("// !$*UTF8*$!\n"));
	}

	@Test
	void writesNonAlphanumericStringWithQuotes() {
		subject.writeStartDocument();
		subject.writeString("a.-?()");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"a.-?()\"")));
	}

	@Test
	void writesStringContainingTabCharacterWithQuotes() {
		subject.writeStartDocument();
		subject.writeString("alpha\t567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha\t567\"")));
	}

	@Test
	void writesStringContainingSpaceCharacterWithQuotes() {
		subject.writeStartDocument();
		subject.writeString("alpha 567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha 567\"")));
	}

	@Test
	void writesStringContainingNewLineCharacterWithQuotes() {
		subject.writeStartDocument();
		subject.writeString("alpha\n567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha\n567\"")));
	}

	@Test
	void writesDictionaryWithSingleStringItem() {
		subject.writeStartDocument();
		subject.writeStartDictionary(1);
		subject.writeDictionaryKey("myKey");
		subject.writeString("my value");
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("{ myKey = \"my value\"; }")));
	}

	@Test
	void writesDictionaryWithMultipleItems() {
		subject.writeStartDocument();
		subject.writeStartDictionary(2);
		subject.writeDictionaryKey("myKey1");
		subject.writeInteger(42);
		subject.writeDictionaryKey("myKey2");
		subject.writeString("my value");
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("{ myKey1 = 42; myKey2 = \"my value\"; }")));
	}

	@Test
	void canWriteDictionaryAsDictionaryItem() {
		subject.writeStartDocument();
		subject.writeStartDictionary(1);
		subject.writeDictionaryKey("myKey1");
		subject.writeStartDictionary(1);
		subject.writeDictionaryKey("myKey2");
		subject.writeString("my value");
		subject.writeEndDictionary();
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("{ myKey1 = { myKey2 = \"my value\"; }; }")));
	}

	@Test
	void writesArrayWithSingleStringItem() {
		subject.writeStartDocument();
		subject.writeStartArray(1);
		subject.writeString("my value");
		subject.writeEndArray();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("( \"my value\" )")));
	}

	@Test
	void writesArrayWithMultipleItems() {
		subject.writeStartDocument();
		subject.writeStartArray(1);
		subject.writeInteger(42);
		subject.writeString("my value");
		subject.writeEndArray();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("( 42, \"my value\" )")));
	}

	@Test
	void canWriteArrayAsArrayItem() {
		subject.writeStartDocument();
		subject.writeStartArray(2);
		subject.writeReal(4.2f);
		subject.writeStartArray(1);
		subject.writeString("my value");
		subject.writeEndArray();
		subject.writeEndArray();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("( \"4.2\", ( \"my value\" ) )")));
	}

	private static String withUTF8Header(String content) {
		return "// !$*UTF8*$!\n" + content;
	}

	@Override
	PropertyListWriter subject() {
		return subject;
	}

	@Override
	void verifySingleTrueValue() {
		assertThat(output(), equalTo(withUTF8Header("true")));
	}

	@Override
	void verifySingleFalseValue() {
		assertThat(output(), equalTo(withUTF8Header("false")));
	}

	@Override
	void verifySingleIntegerValue(int expected) {
		assertThat(output(), equalTo(withUTF8Header(String.valueOf(expected))));
	}

	@Override
	void verifySingleRealValue(float expected) {
		assertThat(output(), equalTo(withUTF8Header("\"" + expected + "\"")));
	}

	@Override
	void verifyAlphanumericWithoutSpaceString(String expected) {
		assertThat(output(), equalTo(withUTF8Header(expected)));
	}

	@Override
	void verifyNonAlphanumericWithoutSpaceString(String expected) {
		assertThat(output(), equalTo(withUTF8Header("\"" + expected + "\"")));
	}

	@Override
	void verifyEmptyDictionary() {
		assertThat(output(), equalTo(withUTF8Header("{}")));
	}

	@Override
	void verifySingleIntegerElementDictionary(Map<String, Object> expected) {
		assertThat(output(), equalTo(withUTF8Header("{ " + expected.entrySet().stream().map(it -> it.getKey() + " = " + value(it.getValue()) + ";").collect(Collectors.joining(" ")) + " }")));
	}

	@Override
	void verifyEmptyArray() {
		assertThat(output(), equalTo(withUTF8Header("()")));
	}

	@Override
	void verifyArray(Object... expectedElements) {
		assertThat(output(), equalTo(withUTF8Header("( " + Arrays.stream(expectedElements).map(it -> value(it)).collect(Collectors.joining(", ")) + " )")));
	}

	@Override
	void verifyEpochDate() {
		assertThat(output(), equalTo(withUTF8Header("\"1970-01-01T00:00:00\"")));
	}

	@Override
	void verifyData_BOOB() {
		assertThat(output(), equalTo(withUTF8Header("<b00b>")));
	}

	private static String value(Object value) {
		if (value.getClass().equals(Integer.class)) {
			return value.toString();
		}
		throw new UnsupportedOperationException();
	}
}
