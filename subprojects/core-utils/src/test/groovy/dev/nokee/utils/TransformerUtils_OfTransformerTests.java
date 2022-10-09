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
package dev.nokee.utils;

import dev.nokee.utils.internal.WrappedTransformer;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class TransformerUtils_OfTransformerTests {
	@Nested
	class WhenSourceTransformerIsGradle {
		Transformer<String, String> sourceTransformer = __ -> "some-value";

		@Test
		void returnsWrappedTransformer() {
			assertThat(TransformerUtils.Transformer.of(sourceTransformer), equalTo(new WrappedTransformer<>(sourceTransformer)));
			assertThat(TransformerUtils.ofTransformer(sourceTransformer), equalTo(new WrappedTransformer<>(sourceTransformer)));
		}
	}

	@Nested
	class WhenSourceTransformerIsEnhanced {
		TransformerUtils.Transformer<String, String> sourceTransformer = __ -> "some-value";

		@Test
		void returnsSameTransformer() {
			assertThat(TransformerUtils.Transformer.of(sourceTransformer), is(sourceTransformer));
			assertThat(TransformerUtils.ofTransformer(sourceTransformer), is(sourceTransformer));
		}
	}
}
