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

import com.google.common.base.Predicates;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.util.Unpacker;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofPredicate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DefaultUnpackingBuilderTests {
	TestDouble<Unpacker> unpacker = newMock(Unpacker.class);
	DefaultUnpackingBuilder<Object> subject = new DefaultUnpackingBuilder<>(unpacker.instance());

	@Test
	void canCreateUnpackExecutableUntil() {
		assertThat(subject.until(Object.class), equalTo(new UnpackerExecutableAdapter<>(new NestableUnpacker(new PredicateBasedUnpacker(unpacker.instance(), new NotPredicate<>(Predicates.instanceOf(Object.class)))))));
	}

	@Test
	void canCreateUnpackExecutableWhileTrue() {
		val predicate = newAlwaysThrowingMock(ofPredicate(Object.class));
		assertThat(subject.whileTrue(predicate), equalTo(new UnpackerExecutableAdapter<>(new NestableUnpacker(new PredicateBasedUnpacker(unpacker.instance(), predicate)))));
	}
}
