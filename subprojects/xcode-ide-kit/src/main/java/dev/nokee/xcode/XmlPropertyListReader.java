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

import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

public final class XmlPropertyListReader implements PropertyListReader, AutoCloseable {
	private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newFactory();
	private final Reader reader;
	private final XMLStreamReader delegate;
	private Context next;
	private Context context;

	public XmlPropertyListReader(Reader reader) {
		this.reader = reader;
		try {
			this.delegate = XML_FACTORY.createXMLStreamReader(reader);
			this.next = findNext();
		} catch (
			XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private interface Context {
		Event event();

		boolean readBoolean();
		long readInteger();
		String readString();
		String readDictionaryKey();
		byte[] readData();
		LocalDateTime readDate();
	}

	private static abstract class ContextAdapter implements Context {
		@Override
		public boolean readBoolean() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long readInteger() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String readString() {
			throw new UnsupportedOperationException(getClass().getSimpleName());
		}

		@Override
		public String readDictionaryKey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] readData() {
			throw new UnsupportedOperationException();
		}

		@Override
		public LocalDateTime readDate() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class DataContext extends ContextAdapter {
		private final String ctx;

		public DataContext(String ctx) {
			this.ctx = ctx;
		}

		@Override
		public Event event() {
			return Event.DATA;
		}

		@Override
		public byte[] readData() {
			final ByteArrayOutputStream result = ctx.chars().filter(it -> !Character.isSpaceChar(it)).map(it -> Character.digit(it, 16)).collect(() -> new ByteArrayOutputStream(ctx.length()), ByteArrayOutputStream::write, (a, b) -> {
				try {
					b.writeTo(a);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			return result.toByteArray();
		}
	}

	private static final class StartDocumentContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DOCUMENT_START;
		}
	}

	private static final class EndDocumentContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DOCUMENT_END;
		}
	}

	private static final class StringContext extends ContextAdapter {
		final String v;

		private StringContext(String v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.STRING;
		}

		@Override
		public String readString() {
			return v;
		}
	}

	private static final class StartArrayContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.ARRAY_START;
		}
	}

	private static final class EndArrayContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.ARRAY_END;
		}
	}

	private static final class StartDictContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DICTIONARY_START;
		}
	}

	private static final class EndDictContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DICTIONARY_END;
		}
	}

	private static final class DictKeyContext extends ContextAdapter {
		final String v;

		private DictKeyContext(String v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.DICTIONARY_KEY;
		}

		@Override
		public String readDictionaryKey() {
			return v;
		}
	}

	private static final class BooleanContext extends ContextAdapter {
		final boolean v;

		private BooleanContext(boolean v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.BOOLEAN;
		}

		@Override
		public boolean readBoolean() {
			return v;
		}
	}

	private static final class DateContext extends ContextAdapter {
		final String v;

		private DateContext(String v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.DATE;
		}

		@Override
		public LocalDateTime readDate() {
			return LocalDateTime.parse(v);
		}
	}

	private static final class IntegerContext extends ContextAdapter {
		final String v;

		private IntegerContext(String v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.INTEGER;
		}

		@Override
		public long readInteger() {
			return Long.parseLong(v);
		}
	}

	@Nullable
	private Context findNext() {
		return get(() -> {
			while (delegate.hasNext()) {
				switch (delegate.next()) {
					case XMLStreamReader.START_ELEMENT:
						switch (delegate.getLocalName()) {
							case "plist": return new StartDocumentContext();
							case "dict": return new StartDictContext();
							case "true": return new BooleanContext(true);
							case "false": return new BooleanContext(false);
							case "array": return new StartArrayContext();
							case "key": return new DictKeyContext(get(delegate::getElementText));
							case "date": return new DateContext(get(delegate::getElementText));
							case "data": return new DataContext(get(delegate::getElementText));
							case "integer": return new IntegerContext(get(delegate::getElementText));
							case "string": return new StringContext(get(delegate::getElementText));
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						switch (delegate.getLocalName()) {
							case "plist": return new EndDocumentContext();
							case "dict": return new EndDictContext();
							case "array": return new EndArrayContext();
						}
						break;
				}
			}

			return null;
		});
	}

	@Override
	public Event next() {
		if (next == null) {
			throw new NoSuchElementException("DOCUMENT_END reached: no more elements on the stream.");
		}

		final Context result = context = next;
		next = findNext();
		return result.event();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String readDictionaryKey() {
		return context.readDictionaryKey();
	}

	@Override
	public byte[] readData() {
		return context.readData();
	}

	@Override
	public LocalDateTime readDate() {
		return context.readDate();
	}

	@Override
	public boolean readBoolean() {
		return context.readBoolean();
	}

	@Override
	public String readString() {
		return context.readString();
	}

	@Override
	public long readInteger() {
		return context.readInteger();
	}

	@Override
	public float readReal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
			reader.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	private static <T> T get(XmlStreamSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private interface XmlStreamSupplier<T> {
		T get() throws XMLStreamException;
	}
}
