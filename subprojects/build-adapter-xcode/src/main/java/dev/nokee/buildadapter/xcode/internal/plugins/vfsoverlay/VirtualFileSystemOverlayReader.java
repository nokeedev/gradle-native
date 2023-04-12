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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.val;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

// https://llvm.org/doxygen/VirtualFileSystem_8h_source.html
public final class VirtualFileSystemOverlayReader implements AutoCloseable {
	private final JsonReader reader;

	public VirtualFileSystemOverlayReader(Reader delegate) {
		this.reader = new JsonReader(delegate);
	}

	public VirtualFileSystemOverlay read() throws IOException {
		List<VirtualFileSystemOverlay.VirtualDirectory> roots = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String fieldName = reader.nextName();
			if (fieldName.equals("roots")) {
				roots = readArray(reader, VirtualFileSystemOverlayReader::readVirtualDirectory);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return new VirtualFileSystemOverlay(roots);
	}

	private static VirtualFileSystemOverlay.VirtualDirectory readVirtualDirectory(JsonReader reader) throws IOException {
		String name = null;
		List<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry> contents = null;

		reader.beginObject();
		while (reader.hasNext()) {
			val fieldName = reader.nextName();
			if (fieldName.equals("name")) {
				name = reader.nextString();
			} else if (fieldName.equals("contents")) {
				contents = readArray(reader, VirtualFileSystemOverlayReader::readRemappedEntry);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return new VirtualFileSystemOverlay.VirtualDirectory(name, contents);
	}

	private static VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry readRemappedEntry(JsonReader reader) throws IOException {
		String name = null;
		String externalContents = null;

		reader.beginObject();
		while (reader.hasNext()) {
			val fieldName = reader.nextName();
			if (fieldName.equals("name")) {
				name = reader.nextString();
			} else if (fieldName.equals("external-contents")) {
				externalContents = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return new VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry(name, externalContents);
	}

	private static <T> List<T> readArray(JsonReader reader, Readable<T> elementReader) throws IOException {
		if (reader.peek().equals(JsonToken.NULL)) {
			return ImmutableList.of();
		} else {
			val builder = ImmutableList.<T>builder();
			reader.beginArray();
			while (reader.hasNext()) {
				builder.add(elementReader.read(reader));
			}
			reader.endArray();

			return builder.build();
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private interface Readable<T> {
		T read(JsonReader reader) throws IOException;
	}
}
