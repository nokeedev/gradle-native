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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class JavaPropertyListWriter implements Closeable {
	private final PropertyListWriter writer;
	private final BiConsumer<? super ValueWriter, ? super Object> rawObjectSerializer;
	private final ValueWriter valueWriter = new ValueWriter();
	private final DictWriter dictWriter = new DictWriter();
	private final ArrayWriter arrayWriter = new ArrayWriter();
	private final ContextPath contextPath = new ContextPath();

	public JavaPropertyListWriter(PropertyListWriter delegate) {
		this(delegate, JavaPropertyListWriter::throwExceptionOnRawObject);
	}

	public JavaPropertyListWriter(PropertyListWriter delegate, BiConsumer<? super ValueWriter, ? super Object> rawObjectSerializer) {
		this.writer = delegate;
		this.rawObjectSerializer = rawObjectSerializer;
	}

	private static void throwExceptionOnRawObject(ValueWriter writer, Object it) {
		throw new UnsupportedOperationException();
	}

	public void writeDocument(PropertyListVersion version, Consumer<? super ValueWriter> action) {
		writer.writeStartDocument(version);
		action.accept(valueWriter);
		writer.writeEndDocument();
	}

	public final class ArrayWriter {
		public void writeArrayElement(int index, Object value) {
			contextPath.with(index, ignored -> valueWriter.writeObject(value));
		}
	}

	public final class DictWriter {
		public void writeKey(String key, Consumer<? super ValueWriter> run) {
			contextPath.with(key, ignored -> {
				writer.writeDictionaryKey(key);
				run.accept(valueWriter);
			});
		}

		public void writeKey(String key, Object value) {
			contextPath.with(key, ignored -> {
				writer.writeDictionaryKey(key);
				valueWriter.writeObject(value);
			});
		}
	}


	public final class ValueWriter {
		public void writeDict(int size, Consumer<? super DictWriter> action) {
			writer.writeStartDictionary(size);
			action.accept(dictWriter);
			writer.writeEndDictionary();
		}

		public void writeArray(int size, Runnable action) {
			writer.writeStartArray(size);
			action.run();
			writer.writeEndArray();
		}

		public void writeObject(Object value) {
			if (value instanceof Double) {
				writer.writeReal((Double) value);
			} else if (value instanceof Number) {
				writer.writeInteger(((Number) value).longValue());
			} else if (value instanceof String) {
				writer.writeString((String) value);
			} else if (value instanceof Boolean) {
				writer.writeBoolean((Boolean) value);
			} else if (value instanceof Collection) {
				if (((Collection<?>) value).isEmpty()) {
					writer.writeEmptyArray();
				} else {
					writer.writeStartArray(((Collection<?>) value).size());
					int i = 0;
					for (Object v : ((Collection<?>) value)) {
						arrayWriter.writeArrayElement(i++, v);
					}
					writer.writeEndArray();
				}
			} else if (value instanceof Map) {
				if (((Map<?, ?>) value).isEmpty()) {
					writer.writeEmptyDictionary();
				} else {
					writer.writeStartDictionary(((Map<?, ?>) value).size());
					for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
						dictWriter.writeKey(entry.getKey().toString(), it -> it.writeObject(entry.getValue()));
					}
					writer.writeEndDictionary();
				}
			} else {
				try{
					rawObjectSerializer.accept(valueWriter, value);
				} catch (Throwable e) {
					throw new RuntimeException(String.format("The value '%s' (%s) could not be written to property list.", contextPath.get(), value.getClass().getSimpleName()), e);
				}
			}

		}
	}

	public void flush() {
		writer.flush();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	private static class ContextPath {
		private final Deque<Object> contextPath = new ArrayDeque<>();

		public <T> void with(T pathSegment, Consumer<? super T> action) {
			contextPath.addLast(pathSegment);
			try {
				action.accept(pathSegment);
			} finally {
				contextPath.removeLast();
			}
		}

		public String get() {
			return contextPath.stream().map(Object::toString).collect(Collectors.joining("."));
		}
	}
}
