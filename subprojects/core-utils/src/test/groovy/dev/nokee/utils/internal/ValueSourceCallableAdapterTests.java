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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSourceCallableAdapterTests {
	@Mock Callable<Object> callable;
	ValueSourceCallableAdapter.Parameters parameters;
	ValueSourceCallableAdapter subject;

	@BeforeEach
	void createSubject() throws Exception {
		when(callable.call()).thenReturn("some-value");
		parameters = objectFactory().newInstance(ValueSourceCallableAdapter.Parameters.class);
		parameters.getCallable().set(callable);
		subject = new ValueSourceCallableAdapter() {
			@Override
			public Parameters getParameters() {
				return parameters;
			}
		};
	}

	@Test
	void returnsCallableValue() {
		MatcherAssert.assertThat(subject.obtain(), equalTo("some-value"));
	}

	@Test
	void forwardsToCallable() throws Exception {
		subject.obtain();
		verify(callable).call();
	}
}
