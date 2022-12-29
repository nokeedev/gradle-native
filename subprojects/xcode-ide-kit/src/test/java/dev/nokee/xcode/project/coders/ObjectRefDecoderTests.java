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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.xcode.project.coders.CoderType.byRef;
import static dev.nokee.xcode.project.coders.WrapDecoder.wrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectRefDecoderTests {
	WrapDecoder<KeyedObject> delegate = new WrapDecoder<>(CoderType.of(KeyedObject.class));
	ObjectRefDecoder<WrapDecoder.Wrapper<KeyedObject>> subject = new ObjectRefDecoder<>(delegate);

	@Nested
	class WhenDecodingObjectCopy {
		ValueDecoder.Context context = new ValueDecoder.Context() {
			@Override
			public KeyedObject decodeBycopyObject(Map<String, ?> object) {
				throw new UnsupportedOperationException();
			}

			@Override
			public KeyedObject decodeByrefObject(Object object) {
				return new RefKeyedObject((String) object);
			}
		};
		String objectToDecode = "a-gid";
		WrapDecoder.Wrapper<KeyedObject> result = subject.decode(objectToDecode, context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(WrapDecoder.wrap(new RefKeyedObject(objectToDecode))));
		}

		@Test
		void callsDelegateWithDecodedObject() {
			assertThat(delegate, calledOnceWith(new RefKeyedObject(objectToDecode), context));
		}
	}

	@Test
	void throwsExceptionIfObjectTypeIsNotString() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(new Object(), new ThrowingDecoderContext()));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(byRef(wrapper(CoderType.of(KeyedObject.class)))));
	}
}
