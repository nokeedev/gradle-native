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

public interface PropertyListReader extends Closeable {
	enum Event {
		DOCUMENT_START,
		DOCUMENT_END,
		DICTIONARY_START,
		DICTIONARY_KEY,
		DICTIONARY_END,
		ARRAY_START,
		ARRAY_END,
		DATA,
		DATE,
		BOOLEAN,
		STRING,
		INTEGER,
		REAL
		;
	}


	Event next();

	boolean hasNext();

	String readDictionaryKey();

	byte[] readData();
	LocalDateTime readDate();
	boolean readBoolean();
	String readString();
	long readInteger();
	double readReal();
}
