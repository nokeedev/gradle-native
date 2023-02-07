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

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.forwarding.ForwardingWrapper;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.util.Unpacker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class NestableUnpackerTests {
	@Nested
	class WhenUnpackingNestedValue {
		TestDouble<Unpacker> delegate = newMock(Unpacker.class).alwaysThrows()
			.when(callTo(method(Unpacker::unpack)).with(args(1)).then(doReturn(2)))
			.when(callTo(method(Unpacker::unpack)).with(args(2)).then(doReturn(3)))
			.when(callTo(method(Unpacker::unpack)).with(args(3)).then(doReturn(4)))
			.when(any(callTo(method(Unpacker::canUnpack))).then(it -> ImmutableSet.of(1, 2, 3).contains(it.getArguments()[0])));
		NestableUnpacker subject = new NestableUnpacker(delegate.instance());
		Object result = subject.unpack(1);

		@Test
		void returnsUnpackedNestedValue() {
			assertThat(result, equalTo(4));
		}

		@Test
		void canUnpack() {
			assertThat(subject.canUnpack(1), equalTo(true));
			assertThat(subject.canUnpack(2), equalTo(true));
			assertThat(subject.canUnpack(3), equalTo(true));
			assertThat(subject.canUnpack(4), equalTo(false));
		}
	}

	@Nested
	class WhenUnpackingNonNestedValue {
		TestDouble<Unpacker> delegate = newMock(Unpacker.class).alwaysThrows()
			.when(any(callTo(method(Unpacker::canUnpack))).then(doReturn(false)));
		NestableUnpacker subject = new NestableUnpacker(delegate.instance());
		Object result = subject.unpack(42);

		@Test
		void returnsTargetValue() {
			assertThat(result, equalTo(42));
		}

		@Test
		void cannotUnpack() {
			assertThat(subject.canUnpack(42), equalTo(false));
		}
	}

	@Test
	void forwardsCanUnpackToDelegate() {
		ForwardingWrapper<Unpacker> subject = ForwardingWrapper.forwarding(Unpacker.class, NestableUnpacker::new);
		assertThat(subject, forwardsToDelegate(method(Unpacker::canUnpack)));
	}
}
