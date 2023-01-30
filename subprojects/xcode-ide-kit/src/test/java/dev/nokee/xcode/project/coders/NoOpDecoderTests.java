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
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class NoOpDecoderTests {
	ValueDecoder.Context context = newAlwaysThrowingMock(ValueDecoder.Context.class);
	NoOpDecoder<String> subject = new NoOpDecoder<>(CoderType.string());

	@Test
	void returnsInputValueWithoutTransformation() {
		assertThat(subject.decode("some-value", context), equalTo("some-value"));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(CoderType.string()));
	}
}
