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

import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.coders.UnwrapEncoder.wrap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ObjectEncoderTests {
	ValueEncoder.Context context = newMock(ValueEncoder.Context.class)
		.when(any(callTo(method(ValueEncoder.Context::encodeBycopyObject))).then(doReturn((it, args) -> new TestBycopyObject(args.getArgument(0)))))
		.alwaysThrows().instance();
	UnwrapEncoder<Encodeable> delegate = new UnwrapEncoder<>();
	ObjectEncoder<UnwrapEncoder.Wrapper<Encodeable>> subject = new ObjectEncoder<>(delegate);

	KeyedObject objectToEncode = new IsaKeyedObject("PBXObject");
	Object result = subject.encode(wrap(objectToEncode), context);

	@Test
	void canEncodeObjectCopy() {
		assertThat(result, equalTo(new TestBycopyObject(objectToEncode)));
	}

	@Test
	void callsDelegateWithInputValue() {
		assertThat(delegate, calledOnceWith(wrap(objectToEncode), context));
	}

	@EqualsAndHashCode
	private static final class TestBycopyObject implements BycopyObject {
		private final Encodeable object;

		public TestBycopyObject(Encodeable object) {
			this.object = object;
		}

		@Override
		public Map<String, ?> asMap() {
			throw new UnsupportedOperationException();
		}
	}
}
