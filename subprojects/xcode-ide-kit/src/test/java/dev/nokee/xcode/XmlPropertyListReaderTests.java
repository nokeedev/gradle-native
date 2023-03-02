/*
 * Copyright 2023 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.text.StringEscapeUtils.escapeXml10;

class XmlPropertyListReaderTests extends PropertyListReaderTester {
	private static XmlPropertyListReader newReader(String... lines) {
		return new XmlPropertyListReader(new InputStreamReader(new ByteArrayInputStream(content(lines))));
	}

	private static byte[] content(String... lines) {
		return Arrays.stream(lines).collect(Collectors.joining(System.lineSeparator())).getBytes(StandardCharsets.UTF_8);
	}

	private static String[] withDoctypeHeader(String... lines) {
		return Stream.concat(Stream.of("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"), Arrays.stream(lines)).toArray(String[]::new);
	}

	private static String[] withPlistVersion1(String... lines) {
		return Stream.concat(Stream.concat(Stream.of("<plist version=\"1.0\">"), Arrays.stream(lines)), Stream.of("</plist>")).toArray(String[]::new);
	}

	@Override
	PropertyListReader newDocument__empty() {
		return newReader(withDoctypeHeader(withPlistVersion1()));
	}

	@Override
	PropertyListReader newDocumentWithBoolean__true() {
		return newReader(withDoctypeHeader(withPlistVersion1("<true/>")));
	}

	@Override
	PropertyListReader newDocumentWithBoolean__false() {
		return newReader(withDoctypeHeader(withPlistVersion1("<false/>")));
	}

	@Override
	PropertyListReader newDocumentWithInteger__26() {
		return newReader(withDoctypeHeader(withPlistVersion1("<integer>26</integer>")));
	}

	@Override
	PropertyListReader newDocumentWithInteger__12612() {
		return newReader(withDoctypeHeader(withPlistVersion1("<integer>12612</integer>")));
	}

	@Override
	PropertyListReader newDocumentWithInteger__272760970() {
		return newReader(withDoctypeHeader(withPlistVersion1("<integer>272760970</integer>")));
	}

	@Override
	PropertyListReader newDocumentWithInteger__2380154602107442436() {
		return newReader(withDoctypeHeader(withPlistVersion1("<integer>2380154602107442436</integer>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_special_456(char special) {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta" + escapeXml10(String.valueOf(special)) + "456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_slash_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta/456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dot_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta.456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_underscore_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta_456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dollarSign_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta$456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_dash_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta-456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_colon_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta:456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__beta_space_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string>beta 456</string>")));
	}

	@Override
	PropertyListReader newDocumentWithString__empty() {
		return newReader(withDoctypeHeader(withPlistVersion1("<string/>")));
	}

	@Override
	PropertyListReader newDocumentWithArray__empty() {
		return newReader(withDoctypeHeader(withPlistVersion1("<array/>")));
	}

	@Override
	PropertyListReader newDocumentWithArray__8706() {
		return newReader(withDoctypeHeader(withPlistVersion1("<array><integer>8706</integer></array>")));
	}

	@Override
	PropertyListReader newDocumentWithArray__myString_9762() {
		return newReader(withDoctypeHeader(withPlistVersion1("<array><string>myString</string><integer>9762</integer></array>")));
	}

	@Override
	PropertyListReader newDocumentWithArray__arrayOf_4_5_6() {
		return newReader(withDoctypeHeader(withPlistVersion1("<array><array><integer>4</integer><integer>5</integer><integer>6</integer></array></array>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__empty() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict/>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__myKey_to_2098176() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>myKey</key><integer>2098176</integer></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__myKey_to_aValue() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>myKey</key><string>aValue</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionary__k0_to_true__k1_to_second__k2_to_3() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>k0</key><true/><key>k1</key><string>second</string><key>k2</key><integer>3</integer></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_special_456(char specialChar) {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta" + escapeXml10(String.valueOf(specialChar)) + "456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_slash_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta/456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dot_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta.456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_underscore_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta_456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dollarSign_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta$456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_dash_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta-456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_colon_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta:456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDictionaryKey__beta_space_456() {
		return newReader(withDoctypeHeader(withPlistVersion1("<dict><key>beta 456</key><string>test</string></dict>")));
	}

	@Override
	PropertyListReader newDocumentWithDate__epoch() {
		return newReader(withDoctypeHeader(withPlistVersion1("<date>1970-01-01T00:00:00.000</date>")));
	}

	@Override
	PropertyListReader newDocumentWithData__c0ffee() {
		return newReader(withDoctypeHeader(withPlistVersion1("<data>c0ffee</data>")));
	}
}
