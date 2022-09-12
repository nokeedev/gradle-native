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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineToolArgumentsBuilderTests {
	CommandLineToolArguments.Builder subject = new CommandLineToolArguments.Builder();

	@Test
	void canBuildEmptyArguments() {
		assertThat(subject.build(), equalTo(new CommandLineToolArgumentsImpl(ImmutableList.of())));
	}

	@Test
	void canBuildArgumentsWithDuplicatedEntries() {
		assertThat(subject.arg("foo").arg("foo").build(), equalTo(new CommandLineToolArgumentsImpl(ImmutableList.of("foo", "foo"))));
	}

	@Test
	void keepArgumentsInSameOrder() {
		assertThat(subject.arg("firstArg").args("secondArg", "thirdArg").arg("fourthArg")
				.args(asList("fifthArg", "sixthArg")).build(),
			equalTo(new CommandLineToolArgumentsImpl(ImmutableList.of("firstArg", "secondArg", "thirdArg", "fourthArg", "fifthArg", "sixthArg"))));
	}

	@Test
	void checkNulls() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> subject.arg(null)),
			() -> assertThrows(NullPointerException.class, () -> subject.args((Object[]) null)),
			() -> assertThrows(NullPointerException.class, () -> subject.args((Iterable<Object>) null)),
			() -> assertThrows(NullPointerException.class, () -> subject.args("firstArg", null))
		);
	}
}
