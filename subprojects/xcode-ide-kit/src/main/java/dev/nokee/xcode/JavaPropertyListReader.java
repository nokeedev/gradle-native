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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_END;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_KEY;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_END;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_START;
import static dev.nokee.xcode.PropertyListReader.Event.STRING;

public final class JavaPropertyListReader implements Closeable {
	private final PropertyListReader reader;
	private final ValueReader valueReader = new ValueReader();
	private final ContextPath contextPath = new ContextPath();

	public JavaPropertyListReader(PropertyListReader delegate) {
		this.reader = delegate;
	}

	public void readDocument(Consumer<? super ValueReader> action) {
		assertNextTag(DOCUMENT_START);
		action.accept(valueReader);
		assertNextTag(DOCUMENT_END);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	public final class ValueReader {
		public Object readObject() {
			return valueOf(reader.next());
		}

		public void skipObject() {
			readObject();
		}

		public Map<String, Object> readDict() {
			assertNextTag(DICTIONARY_START);
			return readDictionaryInternal();
		}

		private Object valueOf(PropertyListReader.Event tag) {
			switch (tag) {
				case DICTIONARY_START:
					return readDictionaryInternal();
				case ARRAY_START:
					final ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
					int i = 0;
					for (PropertyListReader.Event e = reader.next(); e != ARRAY_END; e = reader.next()) {
						final PropertyListReader.Event valueTag = e;
						contextPath.with(i++, () -> listBuilder.add(valueOf(valueTag)));
					}
					return listBuilder.build();
				case BOOLEAN:
					return reader.readBoolean();
				case STRING:
					return reader.readString();
				case INTEGER:
					return reader.readInteger();
				case DATA:
					return reader.readData();
				case DATE:
					return reader.readDate();
				case REAL:
					return reader.readReal();
				default:
					throw new IllegalStateException(String.format("tag <%s> unexpected when reading object at '%s'", tag.name(), contextPath.get()));
			}
		}

		private Map<String, Object> readDictionaryInternal() {
			final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
			for (PropertyListReader.Event e = reader.next(); e != DICTIONARY_END; e = reader.next()) {
				assertEquals(DICTIONARY_KEY, e);
				val key = reader.readDictionaryKey();
				val value = contextPath.with(key, this::readObject);
				mapBuilder.put(key, value);
			}
			return mapBuilder.build();
		}

		public void readDict(BiConsumer<? super String, ? super ValueReader> action) {
			assertEquals(DICTIONARY_START, reader.next());
			for (PropertyListReader.Event e = reader.next(); e != DICTIONARY_END; e = reader.next()) {
				assertEquals(DICTIONARY_KEY, e);
				val key = reader.readDictionaryKey();
				contextPath.with(key, () -> action.accept(key, valueReader));
			}
		}

		public String readString() {
			assertNextTag(STRING);
			return reader.readString();
		}
	}

	private void assertNextTag(PropertyListReader.Event expected) {
		if (!reader.hasNext()) {
			throw new IllegalStateException("expecting <" + expected + "> but end of event stream reached");
		}

		assertEquals(expected, reader.next());
	}

	private static <T> void assertEquals(T expected, T actual) {
		if (!expected.equals(actual)) {
			throw new IllegalStateException("expecting <" + expected + "> but actual is <" + actual + ">");
		}
	}

	private static class ContextPath {
		private final Deque<Object> contextPath = new ArrayDeque<>();

		public void with(Object pathSegment, Runnable action) {
			contextPath.addLast(pathSegment);
			try {
				action.run();
			} finally {
				contextPath.removeLast();
			}
		}

		public Object with(Object pathSegment, Supplier<? extends Object> action) {
			contextPath.addLast(pathSegment);
			try {
				return action.get();
			} finally {
				contextPath.removeLast();
			}
		}

		public String get() {
			return contextPath.stream().map(Object::toString).collect(Collectors.joining("."));
		}
	}
}
