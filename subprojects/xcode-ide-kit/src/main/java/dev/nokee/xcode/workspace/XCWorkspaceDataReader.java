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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class XCWorkspaceDataReader implements Closeable {
	private final XMLStreamReader delegate;

	public XCWorkspaceDataReader(Reader reader) {
		try {
			delegate = XMLInputFactory.newFactory().createXMLStreamReader(reader);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public XCWorkspaceData read() {
		try {
			final XCWorkspaceData.Builder builder = XCWorkspaceData.builder();

			while (delegate.hasNext()) {
				if (delegate.next() == START_ELEMENT && delegate.getLocalName().equals("FileRef")) {
					visitStartFileRef(builder);
				}
			}

			return builder.build();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private void visitStartFileRef(XCWorkspaceData.Builder builder) {
		final String location = delegate.getAttributeValue("", "location");
		builder.fileRef(XCFileReference.of(location));
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
}
