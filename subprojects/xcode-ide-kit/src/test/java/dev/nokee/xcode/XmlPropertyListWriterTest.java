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

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class XmlPropertyListWriterTest extends PropertyListWriterTester {
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private final XmlPropertyListWriter subject = new XmlPropertyListWriter(new OutputStreamWriter(outStream));

	@Test
	void writesPropertyListDTDOnDocumentStart() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeEndDocument();

		assertThat(output(), startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"));
	}

	@Test
	void writesPropertyListRootElementOnDocumentStart() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeEndDocument();

		assertThat(output(), endsWith("<plist version=\"1.0\"></plist>"));
	}

	@Test
	void writesDictionaryWithSingleItem() {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeStartDictionary(1);
		subject.writeDictionaryKey("my-key");
		subject.writeBoolean(true);
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withHeader("<dict><key>my-key</key><true/></dict>")));
	}

	@Test
	void writesDictionaryWithMultipleItems() throws XMLStreamException {
		subject.writeStartDocument(PropertyListVersion.VERSION_00);
		subject.writeStartDictionary(2);
		subject.writeDictionaryKey("my-int");
		subject.writeInteger(42);
		subject.writeDictionaryKey("my-bool");
		subject.writeBoolean(false);
		subject.writeEndDictionary();
		subject.writeEndDocument();

		assertThat(output(), equalTo(withHeader("<dict><key>my-int</key><integer>42</integer><key>my-bool</key><false/></dict>")));
	}

	private String output() {
		return outStream.toString().replaceAll("\n\r?", "\n");
	}

	private static String withHeader(String content) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\"><plist version=\"1.0\">" + content + "</plist>";
	}

	@Override
	PropertyListWriter subject() {
		return subject;
	}

	@Override
	void verifySingleTrueValue() {
		assertThat(output(), equalTo(withHeader("<true/>")));
	}

	@Override
	void verifySingleFalseValue() {
		assertThat(output(), equalTo(withHeader("<false/>")));
	}

	@Override
	void verifySingleIntegerValue(int expected) {
		assertThat(output(), equalTo(withHeader("<integer>" + expected + "</integer>")));
	}

	@Override
	void verifySingleRealValue(float expected) {
		assertThat(output(), equalTo(withHeader("<real>" + expected + "</real>")));
	}

	@Override
	void verifyAlphanumericWithoutSpaceString(String expected) {
		assertThat(output(), equalTo(withHeader("<string>" + expected + "</string>")));
	}

	@Override
	void verifyNonAlphanumericWithoutSpaceString(String expected) {
		assertThat(output(), equalTo(withHeader("<string>" + expected + "</string>")));
	}

	@Override
	void verifyEmptyString() {
		assertThat(output(), equalTo(withHeader("<string></string>")));
	}

	@Override
	void verifyEmptyDictionary() {
		assertThat(output(), equalTo(withHeader("<dict/>")));
	}

	@Override
	void verifySingleIntegerElementDictionary(Map<String, Object> expected) {
		assertThat(output(), equalTo(withHeader("<dict>" + expected.entrySet().stream().map(it -> key(it.getKey()) + value(it.getValue())).collect(Collectors.joining()) + "</dict>")));
	}

	@Override
	void verifyEmptyArray() {
		assertThat(output(), equalTo(withHeader("<array/>")));
	}

	@Override
	void verifyArray(Object... expectedElements) {
		assertThat(output(), equalTo(withHeader("<array>" + Arrays.stream(expectedElements).map(it -> value(it)).collect(Collectors.joining()) + "</array>")));
	}

	@Override
	void verifyEpochDate() {
		assertThat(output(), equalTo(withHeader("<date>1970-01-01T00:00:00</date>")));
	}

	@Override
	void verifyData_BOOB() {
		assertThat(output(), equalTo(withHeader("<data>sAs=</data>")));
	}

	private static String key(String value) {
		return "<key>" + value + "</key>";
	}

	private static String value(Object value) {
		if (value.getClass().equals(Integer.class)) {
			return "<integer>" + value + "</integer>";
		}
		throw new UnsupportedOperationException();
	}
}
