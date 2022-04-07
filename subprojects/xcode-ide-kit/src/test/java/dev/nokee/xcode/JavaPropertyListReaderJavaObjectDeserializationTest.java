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

import lombok.val;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_END;
import static dev.nokee.xcode.PropertyListReader.Event.ARRAY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_END;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_KEY;
import static dev.nokee.xcode.PropertyListReader.Event.DICTIONARY_START;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_END;
import static dev.nokee.xcode.PropertyListReader.Event.DOCUMENT_START;
import static dev.nokee.xcode.PropertyListReader.Event.INTEGER;
import static dev.nokee.xcode.PropertyListReader.Event.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JavaPropertyListReaderJavaObjectDeserializationTest {
	private final List<MutableBoolean> executed = new ArrayList<>();
	@Mock PropertyListReader reader;
	JavaPropertyListReader subject;

	@BeforeEach
	void createSubject() {
		subject = new JavaPropertyListReader(reader);
	}

	@AfterEach
	void assertConsumer() {
		assertTrue(executed.stream().allMatch(MutableBoolean::booleanValue));
	}

	private <T> Consumer<JavaPropertyListReader.ValueReader> assertReadObject(Matcher<T> matcher) {
		MutableBoolean bool = new MutableBoolean();
		executed.add(bool);
		return reader -> {
			bool.setTrue();

			@SuppressWarnings("unchecked")
			val obj = (T) reader.readObject();

			assertThat(obj, matcher);
		};
	}

	@Test
	void readsEmptyDictionary() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, DICTIONARY_START, DICTIONARY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, true, false);
		subject.readDocument(assertReadObject(anEmptyMap()));
	}

	@Test
	void readsEmptyArray() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, ARRAY_START, ARRAY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, true, false);
		subject.readDocument(assertReadObject(emptyIterable()));
	}

	@Test
	void readsString() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, STRING, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		Mockito.when(reader.readString()).thenReturn("my string");
		subject.readDocument(assertReadObject(equalTo("my string")));
	}

	@Test
	void readsInteger() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, INTEGER, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		Mockito.when(reader.readInteger()).thenReturn(42L);
		subject.readDocument(assertReadObject(equalTo(42L)));
	}

	@Test
	void readsArrayWithString() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, ARRAY_START, STRING, STRING, ARRAY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, true, true, true, false);
		Mockito.when(reader.readString()).thenReturn("first", "second");
		subject.readDocument(assertReadObject(contains("first", "second")));
	}

	@Test
	void readsDictionaryWithString() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, DICTIONARY_START, DICTIONARY_KEY, STRING, DICTIONARY_KEY, STRING, DICTIONARY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, true, true, true, true, true, false);
		Mockito.when(reader.readDictionaryKey()).thenReturn("key1", "key2");
		Mockito.when(reader.readString()).thenReturn("first", "second");
		subject.readDocument(assertReadObject(allOf(hasEntry("key1", "first"), hasEntry("key2", "second"))));
	}
}
