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
	void canReadQuotedBoolean() {
		val subject = newReader("\"true\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readBoolean(), equalTo(true));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadQuotedInteger() {
		val subject = newReader("\"42\"");
		assertThat(subject.next(), is(DOCUMENT_START));
		assertThat(subject.next(), is(STRING));
		assertThat(subject.readInteger(), equalTo(42L));
		assertThat(subject.next(), is(DOCUMENT_END));
	}

	@Test
	void canReadArraySplitAcrossMultipleLinesWithTagIndentation() {
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
	PropertyListReader newSingleTrueBooleanReader() {
		return newReader(withUTF8Header("true"));
	}

	@Override
	PropertyListReader newSingleFalseBooleanReader() {
		return newReader(withUTF8Header("false"));
	}

	PropertyListReader.Event integerType() {
		return PropertyListReader.Event.STRING;
	}

	@Override
	PropertyListReader newSingleUInt8Reader_Hex42() {
		return newReader(withUTF8Header("66"));
	}

	@Override
	PropertyListReader newSingleUInt16Reader_Hex4241() {
		return newReader(withUTF8Header("16961"));
	}

	@Override
	PropertyListReader newSingleUInt32Reader_Hex4241999() {
		return newReader(withUTF8Header("69474713"));
	}

	@Override
	PropertyListReader newSingleAlphanumericString_alpha456() {
		return newReader(withUTF8Header("alpha456"));
	}

	@Override
	PropertyListReader newSingleAlphanumericStringWithSpaces_alpha_space_456() {
		return newReader(withUTF8Header("\"alpha 456\""));
	}

	@Override
	PropertyListReader newSingleArray_empty() {
		return newReader(withUTF8Header("()"));
	}

	@Override
	PropertyListReader newSingleArray_hex52() {
		return newReader(withUTF8Header("(82)"));
	}

	@Override
	PropertyListReader newSingleArray__myString_hex98() {
		return newReader(withUTF8Header("(myString, 152)"));
	}

	@Override
	PropertyListReader newSingleDict_empty() {
		return newReader(withUTF8Header("{}"));
	}

	@Override
	PropertyListReader newSingleDict__myKey_to_hex78() {
		return newReader(withUTF8Header("{myKey = 120;}"));
	}
}
