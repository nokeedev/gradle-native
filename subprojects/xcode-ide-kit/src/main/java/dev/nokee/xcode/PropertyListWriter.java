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
import java.time.LocalDateTime;

// TODO: Maybe we should use AutoClosable
// TODO: We should have our own PropertyListException or something similar
public interface PropertyListWriter extends Closeable {
	void writeStartDocument(PropertyListVersion version);
	void writeEndDocument();

	// TODO: Test for negative count
	void writeStartDictionary(long elementCount);
	// TODO: What happen if key has spaces?
	void writeDictionaryKey(String key);
	void writeEndDictionary();
	void writeEmptyDictionary();

	// TODO: Test for negative count
	void writeStartArray(long elementCount);
	void writeEndArray();
	void writeEmptyArray();

	// TODO: Should we rename to simple write(...).
	//   I'm not sure because we should also have writeStringAscii() and writeStringUTF8()
	void writeData(byte[] bytes);
	void writeDate(LocalDateTime date);
	void writeBoolean(boolean b);
	void writeString(CharSequence s);
	void writeInteger(long n);
	void writeReal(double n);

	void flush();
}
