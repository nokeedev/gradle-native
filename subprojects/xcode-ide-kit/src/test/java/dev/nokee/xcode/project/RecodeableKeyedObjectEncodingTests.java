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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.internal.testing.MockitoMethodWrapper.method;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

@ExtendWith(MockitoExtension.class)
class RecodeableKeyedObjectEncodingTests {
	@Mock KeyedObject delegate;
	@Mock CodingKey key;
	RecodeableKeyedObject subject;
	@Mock
	Codeable.EncodeContext context;

	@BeforeEach
	void givenSubject() {
		subject = new RecodeableKeyedObject(delegate, ImmutableSet.of(key));
	}

	@Nested
	class WhenCodingKeyAbsentInDelegate {
		@BeforeEach
		void givenKeyAbsentInDelegate() {
			Mockito.when(delegate.tryDecode(key)).thenReturn(null);
		}

		@Test
		void doesNotEncodeAbsentKey() {
			subject.encode(context);
			assertThat(method(context, Codeable.EncodeContext::tryEncode), calledOnceWith(not(hasKey(key))));
		}
	}

	@Nested
	class WhenCodingKeyPresentInDelegate {
		Object value = new Object();

		@BeforeEach
		void givenKeyPresentInDelegate() {
			Mockito.when(delegate.tryDecode(key)).thenReturn(value);
		}

		@Test
		void doesNotEncodeAbsentKey() {
			subject.encode(context);
			assertThat(method(context, Codeable.EncodeContext::tryEncode), calledOnceWith(hasEntry(key, value)));
		}
	}
}
