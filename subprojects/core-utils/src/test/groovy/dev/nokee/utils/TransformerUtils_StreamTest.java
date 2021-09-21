/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static dev.nokee.utils.TransformerUtils.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TransformerUtils_StreamTest {
	@Test
	void canManipulateStream() {
		assertThat(stream(it -> it.map(Object::toString).collect(joining("-"))).transform(Arrays.asList("foo", "bar")), equalTo("foo-bar"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(stream(it -> it.map(Object::toString).collect(toList())), isA(TransformerUtils.Transformer.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stream(new TestFunction("a")), stream(new TestFunction("a")))
			.addEqualityGroup(stream(new TestFunction("b")))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(stream(new TestFunction("a")), hasToString(startsWith("TransformerUtils.stream(TestFunction(a))")));
	}

	@EqualsAndHashCode
	private static final class TestFunction implements Function<Object, Object> {
		private final String what;

		private TestFunction(String what) {
			this.what = what;
		}

		@Override
		public Object apply(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "TestFunction(" + what + ")";
		}
	}
}
