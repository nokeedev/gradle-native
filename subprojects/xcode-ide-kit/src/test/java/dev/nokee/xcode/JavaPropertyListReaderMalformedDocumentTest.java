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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JavaPropertyListReaderMalformedDocumentTest {
	private final List<MutableBoolean> executed = new ArrayList<>();
	@Mock PropertyListReader reader;
	JavaPropertyListReader subject;

	@BeforeEach
	void createSubject() {
		subject = new JavaPropertyListReader(reader);
	}

	private static <T> Consumer<T> doNothing() {
		return it -> {};
	}

	@Test
	void failsWhenDocumentDoesNotStart() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(doNothing()));
		assertThat(ex.getMessage(), equalTo("expecting <DOCUMENT_START> but actual is <DOCUMENT_END>"));
	}

	@Test
	void failsWhenDocumentDoesNotEnd() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START);
		Mockito.when(reader.hasNext()).thenReturn(true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(doNothing()));
		assertThat(ex.getMessage(), equalTo("expecting <DOCUMENT_END> but end of event stream reached"));
	}

	@Test
	void failsWhenDictionaryDoesNotStartOnObjectRead() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, DICTIONARY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readObject()));
		assertThat(ex.getMessage(), containsString("tag <DICTIONARY_END> unexpected"));
	}

	@Test
	void failsWhenDictionaryDoesNotStartOnDictionaryRead() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, STRING, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readDict()));
		assertThat(ex.getMessage(), equalTo("expecting <DICTIONARY_START> but actual is <STRING>"));
	}

	@Test
	void failsWhenDictionaryDoesNotEnd() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, DICTIONARY_START, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readObject()));
		assertThat(ex.getMessage(), equalTo("expecting <DICTIONARY_KEY> but actual is <DOCUMENT_END>"));
	}

	@Test
	void failsWhenDictionaryHasNoValueForKey() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, DICTIONARY_START, DICTIONARY_KEY, DICTIONARY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, true, true, false);
		Mockito.when(reader.readDictionaryKey()).thenReturn("myKey");
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readObject()));
		assertThat(ex.getMessage(), containsString("tag <DICTIONARY_END> unexpected"));
	}

	@Test
	void failsWhenArrayDoesNotStart() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, ARRAY_END, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readObject()));
		assertThat(ex.getMessage(), containsString("tag <ARRAY_END> unexpected"));
	}

	@Test
	void failsWhenArrayDoesNotEnd() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, ARRAY_START, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readObject()));
		assertThat(ex.getMessage(), containsString("tag <DOCUMENT_END> unexpected"));
	}

	@Test
	void failsWhenNotStringButReadingString() {
		Mockito.when(reader.next()).thenReturn(DOCUMENT_START, INTEGER, DOCUMENT_END);
		Mockito.when(reader.hasNext()).thenReturn(true, true, true, false);
		val ex = assertThrows(IllegalStateException.class, () -> subject.readDocument(it -> it.readString()));
		assertThat(ex.getMessage(), equalTo("expecting <STRING> but actual is <INTEGER>"));
	}
}
