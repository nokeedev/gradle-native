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

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.xcode.project.coders.CoderType.byRef;
import static dev.nokee.xcode.project.coders.CoderType.of;
import static dev.nokee.xcode.project.coders.UnwrapEncoder.wrap;
import static dev.nokee.xcode.project.coders.UnwrapEncoder.wrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ObjectRefEncoderTests {
	ValueEncoder.Context context = new ValueEncoder.Context() {
		@Override
		public BycopyObject encodeBycopyObject(Encodeable object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ByrefObject encodeByrefObject(Encodeable object) {
			return new TestByrefObject(object);
		}
	};
	UnwrapEncoder<Encodeable> delegate = new UnwrapEncoder<>(of(Encodeable.class));
	ObjectRefEncoder<UnwrapEncoder.Wrapper<Encodeable>> subject = new ObjectRefEncoder<>(delegate);

	KeyedObject objectToEncode = new IsaKeyedObject("PBXObject");
	Object result = subject.encode(wrap(objectToEncode), context);

	@Test
	void canEncodeObjectRef() {
		assertThat(result, equalTo(new TestByrefObject(objectToEncode)));
	}

	@Test
	void callsDelegateWithInputValue() {
		assertThat(delegate, calledOnceWith(wrap(objectToEncode), context));
	}

	@Test
	void hasEncodeType() {
		assertThat(subject.getEncodeType(), equalTo(byRef(wrapper(of(Encodeable.class)))));
	}

	@EqualsAndHashCode
	private static final class TestByrefObject implements ByrefObject {
		private final Encodeable object;

		public TestByrefObject(Encodeable object) {
			this.object = object;
		}
	}
}
