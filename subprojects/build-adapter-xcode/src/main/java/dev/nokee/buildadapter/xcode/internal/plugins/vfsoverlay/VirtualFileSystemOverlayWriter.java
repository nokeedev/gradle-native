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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import lombok.val;
import org.apache.commons.io.output.CloseShieldWriter;

import java.io.IOException;
import java.io.Writer;

// https://llvm.org/doxygen/VirtualFileSystem_8h_source.html
public final class VirtualFileSystemOverlayWriter implements AutoCloseable {
	private final Writer delegate;

	public VirtualFileSystemOverlayWriter(Writer delegate) {
		this.delegate = delegate;
	}

	public void write(VirtualFileSystemOverlay overlay) throws IOException {
		try (final JsonWriter writer = new Gson().newJsonWriter(CloseShieldWriter.wrap(delegate))) {
			writer.beginObject();
			writer.name("version").value(0);
			writer.name("case-sensitive").value(Boolean.FALSE.toString()); // it seems this boolean is a string

			writer.name("roots").beginArray();
			for (val root : overlay) {
				writer.beginObject();
				writer.name("type").value("directory");
				writer.name("name").value(root.getName());
				writer.name("contents").beginArray();
				for (val remapped : root) {
					writer.beginObject();
					writer.name("type").value("file");
					writer.name("name").value(remapped.getName());
					writer.name("external-contents").value(remapped.getExternalContents());
					writer.endObject();
				}
				writer.endArray();
				writer.endObject();
			}
			writer.endArray();

			writer.endObject();
		}
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
