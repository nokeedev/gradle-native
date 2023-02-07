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

import dev.nokee.internal.testing.forwarding.ForwardingWrapper;
import dev.nokee.util.Unpacker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.util.internal.DeferredFactory.callableOf;
import static dev.nokee.util.internal.DeferredFactory.throwIfResolved;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class CallableUnpackerTests {
	@Nested
	class WhenUnpackingCallable {
		Unpacker delegate = newAlwaysThrowingMock(Unpacker.class);
		CallableUnpacker subject = new CallableUnpacker(delegate);
		Object result = subject.unpack(callableOf("called-foo"));

		@Test
		void returnCallableValue() {
			assertThat(result, equalTo("called-foo"));
		}

		@Test
		void canUnpack() {
			assertThat(subject.canUnpack(callableOf(throwIfResolved())), equalTo(true));
		}
	}

	@Test
	void canUnpackNullReturningCallable() {
		assertThat(new CallableUnpacker(newAlwaysThrowingMock(Unpacker.class)).unpack(callableOf(null)), nullValue());
	}

	@Nested
	class ForwardAnyValueToDelegate {
		ForwardingWrapper<Unpacker> subject = ForwardingWrapper.forwarding(Unpacker.class, CallableUnpacker::new);

		@Test
		void forwardsUnpackToDelegate() {
			assertThat(subject, forwardsToDelegate(method(Unpacker::unpack)));
		}

		@Test
		void forwardsCanUnpackToDelegate() {
			assertThat(subject, forwardsToDelegate(method(Unpacker::canUnpack)));
		}
	}
}
