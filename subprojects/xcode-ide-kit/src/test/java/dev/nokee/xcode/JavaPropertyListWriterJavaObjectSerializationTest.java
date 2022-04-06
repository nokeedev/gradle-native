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

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static dev.nokee.xcode.PropertyListVersion.VERSION_00;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JavaPropertyListWriterJavaObjectSerializationTest {
	@Mock PropertyListWriter writer;
	JavaPropertyListWriter subject;

	@BeforeEach
	void createSubject() {
		subject = new JavaPropertyListWriter(writer);
	}

	@Test
	void writesEmptyJavaListAsEmptyArray() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(emptyList()));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeEmptyArray();
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesEmptyJavaSetAsEmptyArray() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Collections.emptySet()));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeEmptyArray();
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesNonEmptyJavaListAsNonEmptyList() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Arrays.asList("first", 2, emptyList())));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeStartArray(3);
		inOrder.verify(writer).writeString("first");
		inOrder.verify(writer).writeInteger(2);
		inOrder.verify(writer).writeEmptyArray();
		inOrder.verify(writer).writeEndArray();
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesEmptyJavaMapAsEmptyDictionary() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(emptyMap()));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeEmptyDictionary();
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesNonEmptyJavaMapAsNonEmptyMap() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(ImmutableMap.of("k0", 1, "k1", emptyList(), "k2", "third")));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeStartDictionary(3);
		inOrder.verify(writer).writeDictionaryKey("k0");
		inOrder.verify(writer).writeInteger(1);
		inOrder.verify(writer).writeDictionaryKey("k1");
		inOrder.verify(writer).writeEmptyArray();
		inOrder.verify(writer).writeDictionaryKey("k2");
		inOrder.verify(writer).writeString("third");
		inOrder.verify(writer).writeEndDictionary();
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaStringAsString() {
		subject.writeDocument(VERSION_00, it -> it.writeObject("some string"));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeString("some string");
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaIntegerAsInteger() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Integer.valueOf(42)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeInteger(42);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaLongAsInteger() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Long.valueOf(52)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeInteger(52);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaShortAsInteger() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Short.valueOf((short) 62)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeInteger(62);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaByteAsInteger() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Byte.valueOf((byte) 12)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeInteger(12);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaFloatAsReal() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Float.valueOf(2.2f)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeReal(2.2f);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void writesJavaDoubleAsReal() {
		subject.writeDocument(VERSION_00, it -> it.writeObject(Double.valueOf(3.2)));

		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(VERSION_00);
		inOrder.verify(writer).writeReal(3.2);
		inOrder.verify(writer).writeEndDocument();
	}

	@Test
	void throwsExceptionOnUnknownObjectType() {
		assertThrows(RuntimeException.class, () -> subject.writeDocument(VERSION_00, it -> it.writeObject(new Object())));
	}
}
