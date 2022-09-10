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

		assertThat(output(), equalTo(withUTF8Header("\"\\Ud83d\\Ude0d\"")));
	}

	@Test
	void escapesUnicodeCharacters_plusMinus() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("\u00B1");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"\\U00b1\"")));
	}

	@Test
	void escapesUnicodeCharacters_alertBell() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("MyBell\u0007");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"MyBell\\a\"")));
	}

	@Test
	void escapesUnicodeCharacters_verticalTab() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeString("\u000bMyVerticalTab");
		subject.writeEndDocument();

		assertThat(output(), equalTo(withUTF8Header("\"\\vMyVerticalTab\"")));
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
	void writesUTF8HeaderAtBeginningOfDocument() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeEndDocument();

		assertThat(output(), equalTo("// !$*UTF8*$!\n"));
	}

	private static String withUTF8Header(String content) {
		return "// !$*UTF8*$!\n" + content + "\n";
	}

	@Override
	PropertyListWriter subject() {
		return subject;
	}

	@Override
	void verifyDocument__empty() {
		assertThat(output(), equalTo("// !$*UTF8*$!\n"));
	}

	@Override
	void verifyDocumentWithBoolean__true() {
		assertThat(output(), equalTo(withUTF8Header("true")));
	}

	@Override
	void verifyDocumentWithBoolean__false() {
		assertThat(output(), equalTo(withUTF8Header("false")));
	}

	@Override
	void verifyDocumentWithInteger__34() {
		assertThat(output(), equalTo(withUTF8Header("34")));
	}

	@Override
	void verifyDocumentWithInteger__9216() {
		assertThat(output(), equalTo(withUTF8Header("9216")));
	}

	@Override
	void verifyDocumentWithInteger__541069328() {
		assertThat(output(), equalTo(withUTF8Header("541069328")));
	}

	@Override
	void verifyDocumentWithInteger__2306142076443623952() {
		assertThat(output(), equalTo(withUTF8Header("2306142076443623952")));
	}

	@Override
	void verifyDocumentWithReal__4_2() {
		assertThat(output(), equalTo(withUTF8Header("4.2")));
	}

	@Override
	void verifyDocumentWithString__alpha567() {
		assertThat(output(), equalTo(withUTF8Header("alpha567")));
	}

	@Override
	void verifyDocumentWithString__alpha_special_567(char special) {
		assertThat(output(), equalTo(withUTF8Header("\"alpha" + special + "567\"")));
	}

	@Override
	void verifyDocumentWithString__alpha_slash_567() {
		// slash in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha/567")));
	}

	@Override
	void verifyDocumentWithString__alpha_dot_567() {
		// dot in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha.567")));
	}

	@Override
	void verifyDocumentWithString__alpha_underscore_567() {
		// underscore in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha_567")));
	}

	@Override
	void verifyDocumentWithString__alpha_dollarSign_567() {
		// dollar sign in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha$567")));
	}

	@Override
	void verifyDocumentWithString__alpha_colon_567() {
		// colon in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha:567")));
	}

	@Override
	void verifyDocumentWithString__alpha_dash_567() {
		// dash in string does not require quoting
		assertThat(output(), equalTo(withUTF8Header("alpha-567")));
	}

	@Override
	void verifyDocumentWithString__alpha_space_567() {
		assertThat(output(), equalTo(withUTF8Header("\"alpha 567\"")));
	}

	@Override
	void verifyDocumentWithString__empty() {
		assertThat(output(), equalTo(withUTF8Header("\"\"")));
	}

	@Override
	void verifyDocumentWithDictionary__empty() {
		assertThat(output(), equalTo(withUTF8Header("{}")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_4608() {
		assertThat(output(), equalTo(withUTF8Header("{ aKey = 4608; }")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_myValue() {
		assertThat(output(), equalTo(withUTF8Header("{ aKey = myValue; }")));
	}

	@Override
	void verifyDocumentWithDictionary__k0_to_first__k1_to_2__k2_to_false() {
		assertThat(output(), equalTo(withUTF8Header("{ k0 = first; k1 = 2; k2 = false; }")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_dictOf_myKey_to_myValue() {
		assertThat(output(), equalTo(withUTF8Header("{ aKey = { myKey = myValue; }; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_special_567(char specialChar) {
		assertThat(output(), equalTo(withUTF8Header("{ \"alpha" + specialChar + "567\" = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_slash_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha/567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dot_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha.567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_underscore_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha_567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dollarSign_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha$567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_colon_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha:567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dash_567() {
		assertThat(output(), equalTo(withUTF8Header("{ alpha-567 = test; }")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_space_567() {
		assertThat(output(), equalTo(withUTF8Header("{ \"alpha 567\" = test; }")));
	}

	@Override
	void verifyDocumentWithArray__empty() {
		assertThat(output(), equalTo(withUTF8Header("()")));
	}

	@Override
	void verifyDocumentWithArray__17440() {
		assertThat(output(), equalTo(withUTF8Header("( 17440 )")));
	}

	@Override
	void verifyDocumentWithArray__384_aString() {
		assertThat(output(), equalTo(withUTF8Header("( 384, aString )")));
	}

	@Override
	void verifyDocumentWithArray__arrayOf_0_1_2() {
		assertThat(output(), equalTo(withUTF8Header("( ( 0, 1, 2 ) )")));
	}

	@Override
	void verifyDocumentWithDate__epoch() {
		assertThat(output(), equalTo(withUTF8Header("1970-01-01T00:00:00")));
	}

	@Override
	void verifyDocumentWithData__b00b() {
		assertThat(output(), equalTo(withUTF8Header("<b00b>")));
	}
}
