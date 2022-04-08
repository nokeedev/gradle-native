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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_END;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_START;
import static dev.nokee.xcode.PropertyListReader.Event.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
		return newReader(withUTF8Header("( 4, 5, 6 )"));
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
	PropertyListReader newDocument__empty() {
		return newReader(withUTF8Header());
	}

	@Override
	PropertyListReader.Event dateType() {
		return STRING;
	}

	@Override
	PropertyListReader newDocumentWithDate__epoch() {
		return newReader(withUTF8Header("\"1970-01-01 00:00:00+00:00\""));
	}

	@Override
	PropertyListReader newDocumentWithData__c0ffee() {
		return newReader(withUTF8Header("<c0ffee>"));
	}
}
