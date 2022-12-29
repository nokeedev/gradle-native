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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.called;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.with;
import static dev.nokee.xcode.project.coders.CoderType.listOf;
import static dev.nokee.xcode.project.coders.CoderType.of;
import static dev.nokee.xcode.project.coders.WrapDecoder.wrap;
import static dev.nokee.xcode.project.coders.WrapDecoder.wrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListDecoderTests {
	ValueDecoder.Context context = new ThrowingDecoderContext();
	WrapDecoder<Object> delegate = new WrapDecoder<>(of(Object.class));
	ListDecoder<WrapDecoder.Wrapper<Object>> subject = new ListDecoder<>(delegate);

	@Nested
	class WhenDecodingEmptyList {
		List<WrapDecoder.Wrapper<Object>> result = subject.decode(of(), context);

		@Test
		void returnsEmptyList() {
			assertThat(result, emptyIterable());
		}

		@Test
		void doesNotCallDelegate() {
			assertThat(delegate, neverCalled());
		}
	}

	@Nested
	class WhenDecodingListWithElements {
		List<WrapDecoder.Wrapper<Object>> result = subject.decode(of("first", "second", "third"), context);

		@Test
		void returnsDecodedList() {
			assertThat(result, contains(wrap("first"), wrap("second"), wrap("third")));
		}

		@Test
		void callsDelegateWithEachListElementsInOrder() {
			assertThat(delegate, called(with("first", context), with("second", context), with("third", context)));
		}
	}

	@Test
	void throwsExceptionOnInvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> subject.decode(new Object(), context));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(listOf(wrapper(of(Object.class)))));
	}
}
