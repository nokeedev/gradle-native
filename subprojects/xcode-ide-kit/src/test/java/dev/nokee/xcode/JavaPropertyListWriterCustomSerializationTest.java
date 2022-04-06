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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.xcode.PropertyListVersion.VERSION_00;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JavaPropertyListWriterCustomSerializationTest {
	@Mock PropertyListWriter writer;
	@Mock BiConsumer<JavaPropertyListWriter.ValueWriter, Object> customSerializer;
	JavaPropertyListWriter subject;

	@BeforeEach
	void createSubject() {
		subject = new JavaPropertyListWriter(writer, customSerializer);
	}

	@Test
	void callsCustomSerializerOnUnknownObject() {
		val unknownObject = new Object();
		subject.writeDocument(VERSION_00, it -> it.writeObject(unknownObject));

		verify(customSerializer).accept(notNull(), eq(unknownObject));
	}

	@Test
	void rethrowsExceptionFromCustomSerializer() {
		doThrow(new RuntimeException("My custom serializer.")).when(customSerializer).accept(any(), any());
		val ex = assertThrows(RuntimeException.class, () -> subject.writeDocument(VERSION_00, it -> it.writeObject(new Object())));
		assertThat(ex.getCause().getMessage(), equalTo("My custom serializer."));
	}
}
