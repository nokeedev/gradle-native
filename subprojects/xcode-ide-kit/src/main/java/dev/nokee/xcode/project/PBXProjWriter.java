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
package dev.nokee.xcode.project;

import dev.nokee.xcode.AsciiPropertyListWriter;
import dev.nokee.xcode.JavaPropertyListWriter;
import dev.nokee.xcode.PropertyListVersion;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public final class PBXProjWriter implements Closeable {
	private final JavaPropertyListWriter writer;

	public PBXProjWriter(Writer writer) {
		this.writer = new JavaPropertyListWriter(new AsciiPropertyListWriter(writer, true), PBXProjWriter::writePBXObjectReferenceAsGlobalID);
	}

	private static void writePBXObjectReferenceAsGlobalID(JavaPropertyListWriter.ValueWriter writer, Object obj) {
		if (obj instanceof PBXObjectReference) {
			writer.writeObject(((PBXObjectReference) obj).getGlobalID());
		} else {
			throw new UnsupportedOperationException(String.format("Unknown object of type %s", obj.getClass().getSimpleName()));
		}
	}

	public void write(PBXProj o) {
		writer.writeDocument(PropertyListVersion.VERSION_00, doc -> {
			doc.writeDict(5, dict -> {
				dict.writeKey("archiveVersion", 1);
				dict.writeKey("classes", Collections.emptyMap());
				dict.writeKey("objectVersion",46);
				dict.writeKey("objects", writeObjects(o.getObjects()));
				dict.writeKey("rootObject", o.getRootObject());
			});
		});

		writer.flush();
	}

	private static Consumer<JavaPropertyListWriter.ValueWriter> writeObjects(PBXObjects objects) {
		return writer -> {
			writer.writeDict(objects.size(), entryWriter -> {
				for (PBXObjectReference object : objects) {
					entryWriter.writeKey(object.getGlobalID(), writeObject(object.getFields()));
				}
			});
		};
	}

	private static Consumer<JavaPropertyListWriter.ValueWriter> writeObject(PBXObjectFields fields) {
		return writer -> {
			writer.writeDict(fields.size(), tt -> {
				for (Map.Entry<String, Object> field : fields.entrySet()) {
					tt.writeKey(field.getKey(), field.getValue());
				}
			});
		};
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
