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
import static dev.nokee.util.internal.DeferredFactory.providerOf;
import static dev.nokee.util.internal.DeferredFactory.throwIfResolved;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderUnpackerTests {
	@Nested
	class WhenUnpackingProvider {
		Unpacker delegate = newAlwaysThrowingMock(Unpacker.class);
		ProviderUnpacker subject = new ProviderUnpacker(delegate);
		Object result = subject.unpack(providerOf("provided-foo"));

		@Test
		void returnProviderValue() {
			assertThat(result, equalTo("provided-foo"));
		}

		@Test
		void canUnpack() {
			assertThat(subject.canUnpack(providerOf(throwIfResolved())), equalTo(true));
		}
	}

	@Test
	void throwsExceptionWhenUnpackingAbsentProvider() {
		assertThrows(IllegalStateException.class, () -> new ProviderUnpacker(newAlwaysThrowingMock(Unpacker.class)).unpack(providerOf(null)));
	}

	@Nested
	class ForwardAnyValueToDelegate {
		ForwardingWrapper<Unpacker> subject = ForwardingWrapper.forwarding(Unpacker.class, ProviderUnpacker::new);

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
