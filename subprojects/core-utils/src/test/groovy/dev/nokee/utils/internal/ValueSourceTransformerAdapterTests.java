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
package dev.nokee.utils.internal;

import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.DescribableMatchers.displayName;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ValueSourceTransformerAdapterTests {
	TestDouble<Transformer<String, String>> transformer = newMock(Transformer.class);
	ValueSourceTransformerAdapter.Parameters parameters;
	ValueSourceTransformerAdapter subject;
	Object result;

	@BeforeEach
	void createSubject() {
		transformer.when(any(callTo(method(Transformer<String, String>::transform))).then(doReturn("transformed-value")));
		parameters = objectFactory().newInstance(ValueSourceTransformerAdapter.Parameters.class);
		parameters.getInput().set("some-serializable-value");
		parameters.getTransformer().set(transformer.instance());
		subject = new ValueSourceTransformerAdapter() {
			@Override
			public Parameters getParameters() {
				return parameters;
			}
		};
		result = subject.obtain();
	}

	@Test
	void returnsTransformedValue() {
		assertThat(result, equalTo("transformed-value"));
	}

	@Test
	void forwardsToTransformer() {
		assertThat(transformer.to(method(Transformer<String, String>::transform)), calledOnceWith("some-serializable-value"));
	}

	@Test
	void checkDisplayName() {
		assertThat(subject, displayName("type 'ValueSourceTransformerAdapter'"));
	}

	@Nested
	class WithDisplayName {
		@BeforeEach
		void givenDisplayName() {
			parameters.getDisplayName().set("my custom display name");
		}

		@Test
		void checkDisplayName() {
			assertThat(subject, displayName("my custom display name"));
		}
	}
}
