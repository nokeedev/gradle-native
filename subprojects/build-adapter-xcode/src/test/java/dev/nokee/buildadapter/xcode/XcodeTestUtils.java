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
package dev.nokee.buildadapter.xcode;

import dev.nokee.xcode.PropertyListVersion;
import dev.nokee.xcode.XmlPropertyListWriter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XcodeTestUtils {
	private XcodeTestUtils() {}

	public static Path createValidXcodeInstallation(Path location) {
		return createValidXcodeInstallation(location, "1.2.3");
	}

	public static Path createValidXcodeInstallation(Path location, String version) {
		try {
			Files.createDirectories(location.resolve("Content/Developer"));

//			val outStream = new ByteArrayOutputStream();
			try (val writer = new XmlPropertyListWriter(Files.newBufferedWriter(location.resolve("Content/version.plist")))) {
//			try (val writer = new XmlPropertyListWriter(new OutputStreamWriter(outStream))) {
				writer.writeStartDocument(PropertyListVersion.VERSION_10);
				writer.writeStartDictionary(1);
				writer.writeDictionaryKey("CFBundleShortVersionString");
				writer.writeString(version);
				writer.writeEndDictionary();
				writer.writeEndDocument();
			}
//			System.out.println(outStream.toString());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return location;
	}
}
