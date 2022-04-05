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
		return outStream.toString().replaceAll("\r?\n", "\n");
	}

	@Test
	void escapesUnicodeCharacters_heartEyeEmoji() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("\uD83D\uDE0D");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"\\ud83d\\ude0d\"")));
	}

	@Test
	void escapesUnicodeCharacters_plusMinus() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("\u00B1");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"\\u00b1\"")));
	}

	@Test
	void escapesUnicodeCharacters_backslash() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("c:\\my\\path.txt");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"c:\\\\my\\\\path.txt\"")));
	}

	@Test
	void escapesUnicodeCharacters_newline() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("My multi\nline text");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"My multi\\nline text\"")));
	}

	@Test
	void escapesUnicodeCharacters_carriageReturn() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("My multi\rline text");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"My multi\\rline text\"")));
	}

	@Test
	void escapesUnicodeCharacters_tab() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("Name\tValue");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"Name\\tValue\"")));
	}

	@Test
	void escapesUnicodeCharacters_backspace() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("Hey\b\b\bOh");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"Hey\\b\\b\\bOh\"")));
	}

	@Test
	void escapesUnicodeCharacters_doubleQuote() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("Mister \"the man\" X.");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"Mister \\\"the man\\\" X.\"")));
	}

	@Test
	void writesUTF8HeaderOnDocumentStart() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeEndDocument();

		assertThat(output(), equalTo("// !$*UTF8*$!\n"));
	}

	@Test
	void writesNonAlphanumericStringWithQuotes() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("a.-?()");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"a.-?()\"")));
	}

	@Test
	void writesStringContainingTabCharacterWithQuotes() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("alpha\t567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha\t567\"")));
	}

	@Test
	void writesStringContainingSpaceCharacterWithQuotes() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("alpha 567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha 567\"")));
	}

	@Test
	void writesStringContainingNewLineCharacterWithQuotes() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("alpha\n567");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"alpha\n567\"")));
	}

	@Test
	void writesDictionaryWithSingleStringItem() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeStartDictionary(1);
		subject.writeDictionaryKey("myKey");
		subject.writeString("my value");
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("{ myKey = \"my value\"; }")));
	}

	@Test
	void writesDictionaryWithMultipleItems() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
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
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
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
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeStartArray(1);
		subject.writeString("my value");
		subject.writeEndArray();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("( \"my value\" )")));
	}

	@Test
	void writesArrayWithMultipleItems() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeStartArray(1);
		subject.writeInteger(42);
		subject.writeString("my value");
		subject.writeEndArray();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("( 42, \"my value\" )")));
	}

	@Test
	void canWriteArrayAsArrayItem() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
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
	void verifyEmptyString() {
		assertThat(output(), equalTo(withUTF8Header("\"\"")));
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
