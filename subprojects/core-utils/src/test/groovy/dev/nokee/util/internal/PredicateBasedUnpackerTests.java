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
import dev.nokee.util.Unpacker;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapperEx.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwards;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofPredicate;
import static org.hamcrest.MatcherAssert.assertThat;

class PredicateBasedUnpackerTests {
	@Test
	void forwardsUnpackToUnpacker() {
		assertThat(forwarding(Unpacker.class, this::newSubject), //
			forwards(method(Unpacker::unpack)).toDelegate());
	}

	@Test
	void forwardsCanUnpackToPredicate() {
		assertThat(forwarding(ofPredicate(Object.class), this::newSubject), //
			forwards(method(Unpacker::canUnpack)).to(method(Predicate<Object>::test)));
	}

	private PredicateBasedUnpacker newSubject(Predicate<Object> predicate) {
		return new PredicateBasedUnpacker(newMock(Unpacker.class).instance(), predicate);
	}

	private PredicateBasedUnpacker newSubject(Unpacker unpacker) {
		return new PredicateBasedUnpacker(unpacker, newMock(ofPredicate(Object.class)).instance());
	}
}
