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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.xcode.PropertyListVersion.VERSION_00;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JavaPropertyListWriterContextPathErrorReportTest {
	@Mock PropertyListWriter writer;
	JavaPropertyListWriter subject;

	@BeforeEach
	void createSubject() {
		subject = new JavaPropertyListWriter(writer);
	}

	@Test
	void reportDictionaryKeyInException() {
		val ex = assertThrows(RuntimeException.class, () -> subject.writeDocument(VERSION_00, it -> it.writeObject(ImmutableMap.of("myKey", new Object()))));
		assertThat(ex.getMessage(), containsString("value 'myKey'"));
	}

	@Test
	void reportArrayIndexInException() {
		val ex = assertThrows(RuntimeException.class, () -> subject.writeDocument(VERSION_00, it -> it.writeObject(ImmutableList.of("first", new Object()))));
		assertThat(ex.getMessage(), containsString("value '1'"));
	}

	@Test
	void reportPathOfNestedDictionariesAndArrays() {
		val ex = assertThrows(RuntimeException.class, () -> subject.writeDocument(VERSION_00, it -> it.writeObject(ImmutableMap.of("my", ImmutableMap.of("key", ImmutableList.of("first", "second", ImmutableMap.of("error", new Object())))))));
		assertThat(ex.getMessage(), containsString("value 'my.key.2.error'"));
	}
}
