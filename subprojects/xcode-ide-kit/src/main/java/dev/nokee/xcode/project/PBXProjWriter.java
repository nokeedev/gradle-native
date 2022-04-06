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
import dev.nokee.xcode.PropertyListVersion;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PBXProjWriter implements Closeable {
	private final AsciiPropertyListWriter writer;

	public PBXProjWriter(Writer writer) {
		this.writer = new AsciiPropertyListWriter(writer, true);
	}

	public void write(PBXProj o) {
		writer.writeStartDocument(PropertyListVersion.VERSION_00);
		writeDict(5, () -> {
			writeKey("archiveVersion", 1);
			writeKey("classes", Collections.emptyMap());
			writeKey("objectVersion",46);
			writeKey("objects", () -> {
				writeDict(o.getObjects().size(), () -> {
					for (PBXObjectReference object : o.getObjects()) {
						writeKey(object.getGlobalID(), () -> {
							writeDict(object.getFields().size(), () -> {
								for (Map.Entry<String, Object> field : object.getFields().entrySet()) {
									writeKey(field.getKey(), field.getValue());
								}
							});
						});
					}
				});
			});
			writeKey("rootObject", o.getRootObject());
		});

		writer.writeEndDocument();

		writer.flush();
	}

	private void writeDict(int size, Runnable action) {
		writer.writeStartDictionary(size);
		action.run();
		writer.writeEndDictionary();
	}

	private final ContextPath contextPath = new ContextPath();
	private void writeKey(String key, Runnable run) {
		contextPath.with(key, ignored -> {
			writer.writeDictionaryKey(key);
			run.run();
		});
	}

	private void writeKey(String key, Object value) {
		contextPath.with(key, ignored -> {
			writer.writeDictionaryKey(key);
			write(value);
		});
	}

	private void writeArrayElement(int index, Object value) {
		contextPath.with(index, ignored -> write(value));
	}

	private void write(Object value) {
		if (value instanceof Double) {
			writer.writeReal((Double) value);
		} else if (value instanceof Number) {
			writer.writeInteger(((Number) value).longValue());
		} else if (value instanceof String) {
			writer.writeString((String) value);
		} else if (value instanceof Boolean) {
			writer.writeBoolean((Boolean) value);
		} else if (value instanceof List) {
			if (((List<?>) value).isEmpty()) {
				writer.writeEmptyArray();
			} else {
				writer.writeStartArray(((List<?>) value).size());
				int i = 0;
				for (Object v : ((List<?>) value)) {
					writeArrayElement(i, v);
				}
				writer.writeEndArray();
			}
		} else if (value instanceof Map) {
			if (((Map<?, ?>) value).isEmpty()) {
				writer.writeEmptyDictionary();
			} else {
				writer.writeStartDictionary(((Map<?, ?>) value).size());
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					writeKey(entry.getKey().toString(), () -> write(entry.getValue()));
				}
				writer.writeEndDictionary();
			}
		} else {
			throw new RuntimeException(contextPath.get() + " - " + value.getClass().toString());
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	private static class ContextPath {
		private final Queue<Object> contextPath = new ArrayDeque<>();

		public <T> void with(T pathSegment, Consumer<? super T> action) {
			contextPath.add(pathSegment);
			try {
				action.accept(pathSegment);
			} finally {
				contextPath.remove();
			}
		}

		public String get() {
			return contextPath.stream().map(Object::toString).collect(Collectors.joining("."));
		}
	}
}
