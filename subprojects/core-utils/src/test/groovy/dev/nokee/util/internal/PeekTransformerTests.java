/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.util.internal;

import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class PeekTransformerTests {
	TestDouble<Consumer<String>> action = newMock(Consumer.class);
	PeekTransformer<String> subject = new PeekTransformer<>(action.instance());
	String result = subject.transform("foo");

	@Test
	void callsActionWithTransformInput() {
		assertThat(action.to(method(Consumer<String>::accept)), calledOnceWith("foo"));
	}

	@Test
	void returnsTransformInput() {
		assertThat(result, equalTo("foo"));
	}
}
