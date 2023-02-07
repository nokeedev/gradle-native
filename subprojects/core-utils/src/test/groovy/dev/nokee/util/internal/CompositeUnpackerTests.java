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

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CompositeUnpackerTests {
	TestDouble<Unpacker> first = newMock(Unpacker.class)
		.when(callTo(method(Unpacker::unpack)).with(args("start")).then(doReturn("middle")))
		.when(callTo(method(Unpacker::canUnpack)).with(args("start")).then(doReturn(true)));
	TestDouble<Unpacker> second = newMock(Unpacker.class)
		.when(callTo(method(Unpacker::unpack)).with(args("middle")).then(doReturn("end")))
		.when(callTo(method(Unpacker::canUnpack)).with(args("middle")).then(doReturn(true)));
	CompositeUnpacker subject = new CompositeUnpacker(first.instance(), second.instance());
	Object result = subject.unpack("start");

	@Test
	void returnsUnpackedValueFromFirstFollowedBySecond() {
		assertThat(result, equalTo("end"));
	}

	@Test
	void canUnpack() {
		assertThat(subject.canUnpack("start"), equalTo(true));
		assertThat(subject.canUnpack("middle"), equalTo(true));
		assertThat(subject.canUnpack("end"), equalTo(false));
		assertThat(subject.canUnpack("42"), equalTo(false));
	}
}
