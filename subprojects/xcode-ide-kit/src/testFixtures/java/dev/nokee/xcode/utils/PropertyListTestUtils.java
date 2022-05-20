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
package dev.nokee.xcode.utils;

import dev.nokee.xcode.AsciiPropertyListWriter;
import dev.nokee.xcode.JavaPropertyListWriter;
import dev.nokee.xcode.PropertyListVersion;
import dev.nokee.xcode.XmlPropertyListWriter;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PropertyListTestUtils {
	public static void writeXmlPlistTo(Object o, Path path) throws IOException {
		try (val writer = new JavaPropertyListWriter(new XmlPropertyListWriter(Files.newBufferedWriter(path)))) {
			writer.writeDocument(PropertyListVersion.VERSION_10, it -> it.writeObject(o));
		}
	}

	public static void writeAsciiPlistTo(Object o, Path path) throws IOException {
		try (val writer = new JavaPropertyListWriter(new AsciiPropertyListWriter(Files.newBufferedWriter(path)))) {
			writer.writeDocument(PropertyListVersion.VERSION_00, it -> it.writeObject(o));
		}
	}
}
