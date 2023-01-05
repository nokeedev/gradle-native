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

import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.utils.ThrowingDecoderContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DictionaryDecoderTests {
	ValueDecoder.Context context = new ThrowingDecoderContext();
	WrapDecoder<Map<String, ?>> delegate = new WrapDecoder<>(CoderType.dict());
	DictionaryDecoder<WrapDecoder.Wrapper<Map<String, ?>>> subject = new DictionaryDecoder<>(delegate);

	@Nested
	class WhenDecodingMap {
		WrapDecoder.Wrapper<Map<String, ?>> result = subject.decode(of("K1", "V1"), context);

		@Test
		void canDecode() {
			assertThat(result, equalTo(WrapDecoder.wrap(of("K1", "V1"))));
		}

		@Test
		void callsDelegateWithMapValue() {
			assertThat(delegate, calledOnceWith(of("K1", "V1"), context));
		}
	}

	@Test
	void throwsExceptionOnUnexpectedValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(42, context));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(WrapDecoder.wrapper(CoderType.dict())));
	}
}
