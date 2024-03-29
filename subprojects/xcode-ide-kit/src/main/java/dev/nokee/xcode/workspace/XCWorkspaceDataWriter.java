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
package dev.nokee.xcode.workspace;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public final class XCWorkspaceDataWriter implements Closeable {
	private final Writer writer;
	private final XMLStreamWriter delegate;

	public XCWorkspaceDataWriter(Writer writer) {
		this.writer = writer;
		try {
			this.delegate = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(XCWorkspaceData o) {
		try {
			delegate.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
			delegate.writeStartElement("Workspace");
			delegate.writeAttribute("version", "1.0");

			for (XCFileReference fileRef : o.getFileRefs()) {
				delegate.writeStartElement("FileRef");
				delegate.writeAttribute("location", fileRef.getLocation());
				delegate.writeEndElement();
			}
			delegate.writeEndElement();
			delegate.writeEndDocument();

			delegate.flush(); // ensure the data is available
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
			writer.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
}
