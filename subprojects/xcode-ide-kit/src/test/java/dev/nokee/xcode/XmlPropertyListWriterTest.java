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
	void verifyDocument__empty() {
		assertThat(output(), equalTo(withHeader("")));
	}

	@Override
	void verifyDocumentWithBoolean__true() {
		assertThat(output(), equalTo(withHeader("<true/>")));
	}

	@Override
	void verifyDocumentWithBoolean__false() {
		assertThat(output(), equalTo(withHeader("<false/>")));
	}

	@Override
	void verifyDocumentWithInteger__34() {
		assertThat(output(), equalTo(withHeader("<integer>34</integer>")));
	}

	@Override
	void verifyDocumentWithInteger__9216() {
		assertThat(output(), equalTo(withHeader("<integer>9216</integer>")));
	}

	@Override
	void verifyDocumentWithInteger__541069328() {
		assertThat(output(), equalTo(withHeader("<integer>541069328</integer>")));
	}

	@Override
	void verifyDocumentWithInteger__2306142076443623952() {
		assertThat(output(), equalTo(withHeader("<integer>2306142076443623952</integer>")));
	}

	@Override
	void verifyDocumentWithReal__4_2() {
		assertThat(output(), equalTo(withHeader("<real>4.2</real>")));
	}

	@Override
	void verifyDocumentWithString__alpha567() {
		assertThat(output(), equalTo(withHeader("<string>alpha567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_special_567(char special) {
		assertThat(output(), equalTo(withHeader("<string>alpha" + special + "567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_slash_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha/567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_dot_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha.567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_underscore_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha_567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_dollarSign_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha$567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_colon_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha:567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_dash_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha-567</string>")));
	}

	@Override
	void verifyDocumentWithString__alpha_space_567() {
		assertThat(output(), equalTo(withHeader("<string>alpha 567</string>")));
	}

	@Override
	void verifyDocumentWithString__empty() {
		assertThat(output(), equalTo(withHeader("<string></string>")));
	}

	@Override
	void verifyDocumentWithDictionary__empty() {
		assertThat(output(), equalTo(withHeader("<dict/>")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_4608() {
		assertThat(output(), equalTo(withHeader("<dict><key>aKey</key><integer>4608</integer></dict>")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_myValue() {
		assertThat(output(), equalTo(withHeader("<dict><key>aKey</key><string>myValue</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionary__k0_to_first__k1_to_2__k2_to_false() {
		assertThat(output(), equalTo(withHeader("<dict><key>k0</key><string>first</string><key>k1</key><integer>2</integer><key>k2</key><false/></dict>")));
	}

	@Override
	void verifyDocumentWithDictionary__aKey_to_dictOf_myKey_to_myValue() {
		assertThat(output(), equalTo(withHeader("<dict><key>aKey</key><dict><key>myKey</key><string>myValue</string></dict></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_special_567(char specialChar) {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha" + specialChar + "567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_slash_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha/567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dot_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha.567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_underscore_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha_567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dollarSign_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha$567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_colon_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha:567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_dash_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha-567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithDictionaryKey__alpha_space_567() {
		assertThat(output(), equalTo(withHeader("<dict><key>alpha 567</key><string>test</string></dict>")));
	}

	@Override
	void verifyDocumentWithArray__empty() {
		assertThat(output(), equalTo(withHeader("<array/>")));
	}

	@Override
	void verifyDocumentWithArray__17440() {
		assertThat(output(), equalTo(withHeader("<array><integer>17440</integer></array>")));
	}

	@Override
	void verifyDocumentWithArray__384_aString() {
		assertThat(output(), equalTo(withHeader("<array><integer>384</integer><string>aString</string></array>")));
	}

	@Override
	void verifyDocumentWithArray__arrayOf_0_1_2() {
		assertThat(output(), equalTo(withHeader("<array><array><integer>0</integer><integer>1</integer><integer>2</integer></array></array>")));
	}

	@Override
	void verifyDocumentWithDate__epoch() {
		assertThat(output(), equalTo(withHeader("<date>1970-01-01T00:00:00</date>")));
	}

	@Override
	void verifyDocumentWithData__b00b() {
		assertThat(output(), equalTo(withHeader("<data>sAs=</data>")));
	}
}
