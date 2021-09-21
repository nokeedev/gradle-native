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
import dev.nokee.internal.testing.ExecuteWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.utils.SpecTestUtils.aSpec;
import static dev.nokee.utils.SpecTestUtils.anotherSpec;
import static dev.nokee.utils.SpecUtils.*;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class SpecUtils_ComposeTest {
	@Test
	void checkToString() {
		assertThat(compose(aSpec(), aTransformer()),
			hasToString("SpecUtils.compose(aSpec(), aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(aSpec(), aTransformer()), compose(aSpec(), aTransformer()))
			.addEqualityGroup(compose(anotherSpec(), aTransformer()))
			.addEqualityGroup(compose(aSpec(), anotherTransformer()))
			.testEquals();
	}

	@Test
	void returnsSatisfyAllWhenSpecifiedSpecIsSatisfyAll() {
		assertThat(compose(satisfyAll(), aTransformer()), equalTo(satisfyAll()));
	}

	@Test
	void returnsSatisfyNoneWhenSpecifiedSpecIsSatisfyNone() {
		assertThat(compose(satisfyNone(), aTransformer()), equalTo(satisfyNone()));
	}

	@Test
	void returnsSpecWhenSpecifiedTransformIsNoOp() {
		assertThat(compose(aSpec(), noOpTransformer()), equalTo(aSpec()));
	}

	@Nested
	class Execution {
		private final Object INPUT = new Object();
		private final Object TRANSFORMER_OUTPUT = new Object();
		private ExecuteWith.ExecutionResult<Object> transformerExecution;
		private ExecuteWith.ExecutionResult<Object> specExecution;

		@BeforeEach
		void setUpComposeAction() {
			specExecution = executeWith(spec(g -> {
				transformerExecution = executeWith(transformer(f -> {
					compose(g, f).isSatisfiedBy(INPUT);
				}).thenReturn(TRANSFORMER_OUTPUT));
			}));
		}

		@Test
		void canTransformInputBeforeCallingSpec() {
			assertThat(specExecution, calledOnceWith(TRANSFORMER_OUTPUT));
			assertThat(transformerExecution, calledOnceWith(INPUT));
		}
	}
}
