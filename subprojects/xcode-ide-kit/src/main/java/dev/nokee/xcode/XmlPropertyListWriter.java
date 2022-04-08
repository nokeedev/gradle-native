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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public final class XmlPropertyListWriter implements PropertyListWriter, AutoCloseable {
	private final XMLStreamWriter delegate;

	public XmlPropertyListWriter(XMLStreamWriter delegate) {
		this.delegate = delegate;
	}

	public XmlPropertyListWriter(Writer delegate) {
		try {
			this.delegate = XMLOutputFactory.newFactory().createXMLStreamWriter(delegate);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeStartDocument(PropertyListVersion version) {
		run(() -> {
			delegate.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
			delegate.writeDTD("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
			delegate.writeStartElement("plist");
			delegate.writeAttribute("version", "1.0");
		});
	}

	@Override
	public void writeEndDocument() {
		run(() -> {
			delegate.writeEndElement(); // plist
			delegate.writeEndDocument();
			delegate.flush();
		});
	}

	@Override
	public void writeStartDictionary(long elementCount) {
		run(() -> {
			delegate.writeStartElement("dict");
		});
	}

	@Override
	public void writeDictionaryKey(String key) {
		run(() -> {
			delegate.writeStartElement("key");
			delegate.writeCharacters(key);
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeEndDictionary() {
		run(() -> {
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeEmptyDictionary() {
		run(() -> {
			delegate.writeEmptyElement("dict");
		});
	}

	@Override
	public void writeStartArray(long elementCount) {
		run(() -> {
			delegate.writeStartElement("array");
		});
	}

	@Override
	public void writeEndArray() {
		run(() -> {
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeEmptyArray() {
		run(() -> {
			delegate.writeEmptyElement("array");
		});
	}

	@Override
	public void writeData(byte[] bytes) {
		run(() -> {
			delegate.writeStartElement("data");
			// TODO: Maybe wrap the encoded data: https://github.com/python/cpython/blob/3.10/Lib/plistlib.py#L365
			delegate.writeCharacters(Base64.getEncoder().encodeToString(bytes));
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeDate(LocalDateTime date) {
		run(() -> {
			delegate.writeStartElement("date");
			delegate.writeCharacters(DateTimeFormatter.ISO_DATE_TIME.format(date));
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeReal(float n) {
		run(() -> {
			delegate.writeStartElement("real");
			delegate.writeCharacters(String.valueOf(n));
			delegate.writeEndElement();
		});
	}

	@Override
	public void flush() {
		run(delegate::flush);
	}

	@Override
	public void writeInteger(long n) {
		run(() -> {
			delegate.writeStartElement("integer");
			delegate.writeCharacters(String.valueOf(n));
			delegate.writeEndElement();
		});
	}

	@Override
	public void writeBoolean(boolean b) {
		run(() -> {
			delegate.writeEmptyElement(String.valueOf(b));
		});
	}

	@Override
	public void writeString(CharSequence s) {
		run(() -> {
			delegate.writeStartElement("string");
			delegate.writeCharacters(s.toString());
			delegate.writeEndElement();
		});
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	private void run(XmlStreamRunnable runnable) {
		try {
			runnable.run();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private interface XmlStreamRunnable {
		void run() throws XMLStreamException;
	}
}
