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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DATA;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_END;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_KEY;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_END;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_START;
import static dev.nokee.xcode.PropertyListReader.Event.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsciiPropertyListReaderTest extends PropertyListReaderTester {
	private static AsciiPropertyListReader newReader(String... lines) {
		return new AsciiPropertyListReader(new InputStreamReader(new ByteArrayInputStream(content(lines))));
	}

	private static byte[] content(String... lines) {
		return Arrays.stream(lines).collect(Collectors.joining(System.lineSeparator())).getBytes(StandardCharsets.UTF_8);
	}

	private static String[] withUTF8Header(String... lines) {
		return Stream.concat(Stream.of("// !$*UTF8*$!"), Arrays.stream(lines)).toArray(String[]::new);
	}

	@Test
	void canReadDocumentWithQuotedBoolean() {
		val subject = newReader("\"true\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readBoolean(), equalTo(true));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDocumentWithQuotedInteger() {
		val subject = newReader("\"42\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readInteger(), equalTo(42L));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDocumentWhenArraySplitAcrossMultipleLinesWithTagIndentation() {
		val subject = newReader("(", "\ta,", "\tb,", "\tc,", ")");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(ARRAY_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("a"));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("b"));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("c"));
		assertThat(subject.next(), is(ARRAY_END));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@ParameterizedTest
	@ValueSource(chars = { 'u', 'U' })
	void unescapesUnicodeCharacters_heartEyeEmoji(char u) {
		val subject = newReader("\"\\" + u + "d83d\\" + u + "de0d\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\ud83d\ude0d"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@ParameterizedTest
	@ValueSource(chars = { 'u', 'U' })
	void unescapesUnicodeCharacters_plusMinus(char u) {
		val subject = newReader("\"\\" + u + "00b1\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u00b1"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_alertBell() {
		val subject = newReader("\"MyBell\\a\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("MyBell\u0007")); // cannot use \a [1]
		assertThat(subject.next(), is(DOCUMENT_END));

		// [1] see https://docs.oracle.com/javase/specs/jls/se11/html/jls-3.html#jls-3.10.6
	}

	@Test
	void unescapesUnicodeCharacters_verticalTab() {
		val subject = newReader("\"\\vMyVerticalTab\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u000bMyVerticalTab")); // cannot use \v [2]
		assertThat(subject.next(), is(DOCUMENT_END));

		// [2] see https://docs.oracle.com/javase/specs/jls/se11/html/jls-3.html#jls-3.10.6
	}

	@Test
	void unescapesUnicodeCharacters_backslash() {
		val subject = newReader("\"c:\\\\my\\\\path.txt\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("c:\\my\\path.txt"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_newline() {
		val subject = newReader("\"My multi\\nline text\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("My multi\nline text"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_carriageReturn() {
		val subject = newReader("\"My multi\\rline text\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("My multi\rline text"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_tab() {
		val subject = newReader("\"Name\\tValue\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("Name\tValue"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_backspace() {
		val subject = newReader("\"Hey\\b\\b\\bOh\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("Hey\b\b\bOh"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_doubleQuote() {
		val subject = newReader("\"Mister \\\"the man\\\" X.\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("Mister \"the man\" X."));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_singleQuote() {
		val subject = newReader("\"Mister \\'the man\\' X.\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("Mister 'the man' X."));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith0() {
		val subject = newReader("\"\\010\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\b"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith1() {
		val subject = newReader("\"\\106\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("F"));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith2() {
		val subject = newReader("\"\\220\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u0090")); // capital letter e with acute
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith3() {
		val subject = newReader("\"\\317\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u00cf")); // single up and double horizontal
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith4() {
		val subject = newReader("\"\\447\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u0127")); // latin small letter h with stroke
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith5() {
		val subject = newReader("\"\\556\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u016e")); // latin capital letter u with ring above
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith6() {
		val subject = newReader("\"\\612\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u018a")); // latin capital letter d with hook
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void unescapesUnicodeCharacters_octalStartsWith7() {
		val subject = newReader("\"\\735\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("\u01dd")); // latin small letter turned e
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDataWithSpaces() {
		val subject = newReader("<b00b b0b c0ffee>");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(DATA));
		assertArrayEquals(new byte[] { (byte) 0xb, (byte) 0x0, (byte) 0x0, (byte) 0xb, (byte) 0xb, (byte) 0x0, (byte) 0xb, (byte) 0xc, (byte) 0x0, (byte) 0xf, (byte) 0xf, (byte) 0xe, (byte) 0xe }, subject.readData());
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDataWithUpperCaseHexDigit() {
		val subject = newReader("<B0B>");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(DATA));
		assertArrayEquals(new byte[] { (byte) 0xb, (byte) 0x0, (byte) 0xb }, subject.readData());
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDataWithFrontSpaces() {
		val subject = newReader("<   B0B>");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(DATA));
		assertArrayEquals(new byte[] { (byte) 0xb, (byte) 0x0, (byte) 0xb }, subject.readData());
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDataWithTailSpaces() {
		val subject = newReader("<B0B   >");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(DATA));
		assertArrayEquals(new byte[] { (byte) 0xb, (byte) 0x0, (byte) 0xb }, subject.readData());
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadDictionaryWithQuotedKeyAndValue() {
		val subject = newReader("{ \"key\" = \"value\"; }");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(DICTIONARY_START));
		assertThat(subject.next(), is(DICTIONARY_KEY));
		assertThat(subject.readDictionaryKey(), equalTo("key"));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readString(), equalTo("value"));
		assertThat(subject.next(), is(DICTIONARY_END));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void throwsExceptionOnParsingError() {
		val ex = assertThrows(RuntimeException.class, () -> newReader("{ aKey = aValue;"));
		assertThat(ex.getMessage(), equalTo("line 1:16 extraneous input '<EOF>' expecting {'}', StringLiteral}"));
	}

	@Override
	PropertyListReader.Event booleanType() {
		return PropertyListReader.Event.STRING;
	}

	@Override
	PropertyListReader newDocumentWithBoolean__true() {
		return newReader(withUTF8Header("true"));
	}

	@Override
	PropertyListReader newDocumentWithBoolean__false() {
		return newReader(withUTF8Header("false"));
	}

	PropertyListReader.Event integerType() {
		return PropertyListReader.Event.STRING;
	}

	@Override
	PropertyListReader newDocumentWithInteger__26() {
		return newReader(withUTF8Header("26"));
	}

	@Override
	PropertyListReader newDocumentWithInteger__12612() {
		return newReader(withUTF8Header("12612"));
	}

	@Override
	PropertyListReader newDocumentWithInteger__272760970() {
		return newReader(withUTF8Header("272760970"));
	}

	@Override
	PropertyListReader newDocumentWithInteger__2380154602107442436() {
		return newReader(withUTF8Header("2380154602107442436"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta456() {
		return newReader(withUTF8Header("beta456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_special_456(char special) {
		return newReader(withUTF8Header("\"beta" + special + "456\""));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_slash_456() {
		return newReader(withUTF8Header("beta/456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dot_456() {
		return newReader(withUTF8Header("beta.456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_underscore_456() {
		return newReader(withUTF8Header("beta_456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dollarSign_456() {
		return newReader(withUTF8Header("beta$456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dash_456() {
		return newReader(withUTF8Header("beta-456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_colon_456() {
		return newReader(withUTF8Header("beta:456"));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_space_456() {
		return newReader(withUTF8Header("\"beta 456\""));
	}

	@Override
	PropertyListReader newDocumentWithString__empty() {
		return newReader(withUTF8Header("\"\""));
	}

	@Override
	PropertyListReader newDocumentWithArray__empty() {
		return newReader(withUTF8Header("()"));
	}

	@Override
	PropertyListReader newDocumentWithArray__8706() {
		return newReader(withUTF8Header("( 8706 )"));
	}

	@Override
	PropertyListReader newDocumentWithArray__myString_9762() {
		return newReader(withUTF8Header("( myString, 9762 )"));
	}

	@Override
	PropertyListReader newDocumentWithArray__arrayOf_4_5_6() {
		return newReader(withUTF8Header("( ( 4, 5, 6 ) )"));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__empty() {
		return newReader(withUTF8Header("{}"));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__myKey_to_2098176() {
		return newReader(withUTF8Header("{ myKey = 2098176; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__myKey_to_aValue() {
		return newReader(withUTF8Header("{ myKey = aValue; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__k0_to_true__k1_to_second__k2_to_3() {
		return newReader(withUTF8Header("{ k0 = true; k1 = second; k2 = 3; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta456() {
		return newReader(withUTF8Header("{ beta456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_special_456(char specialChar) {
		return newReader(withUTF8Header("{ \"beta" + specialChar + "456\" = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_slash_456() {
		return newReader(withUTF8Header("{ beta/456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dot_456() {
		return newReader(withUTF8Header("{ beta.456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_underscore_456() {
		return newReader(withUTF8Header("{ beta_456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dollarSign_456() {
		return newReader(withUTF8Header("{ beta$456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dash_456() {
		return newReader(withUTF8Header("{ beta-456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_colon_456() {
		return newReader(withUTF8Header("{ beta:456 = test; }"));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_space_456() {
		return newReader(withUTF8Header("{ \"beta 456\" = test; }"));
	}

	@Override
	PropertyListReader newDocument__empty() {
		return newReader(withUTF8Header());
	}

	@Override
	PropertyListReader.Event dateType() {
		return STRING;
	}

	@Override
	PropertyListReader newDocumentWithDate__epoch() {
		return newReader(withUTF8Header("\"1970-01-01T00:00:00.000\""));
	}

	@Override
	PropertyListReader newDocumentWithData__c0ffee() {
		return newReader(withUTF8Header("<c0ffee>"));
	}
}
