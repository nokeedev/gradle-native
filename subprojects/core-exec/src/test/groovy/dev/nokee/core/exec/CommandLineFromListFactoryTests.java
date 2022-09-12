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
import dev.nokee.core.exec.internal.DefaultCommandLine;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;

import static dev.nokee.core.exec.CommandLineToolArguments.empty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineFromListFactoryTests {
	abstract class Tester {
		abstract CommandLine createSubject(List<?> values);

		@Test
		void canCreateCommandLineFromExecutableOnly() {
			assertThat(createSubject(singletonList("my-executable")),
				equalTo(new DefaultCommandLine(CommandLineTool.of("my-executable"), empty())));
		}

		@Test
		void canCreateCommandLineFromExecutableAndArguments() {
			assertThat(createSubject(asList("my-executable", "firstArg", "secondArg")),
				equalTo(new DefaultCommandLine(CommandLineTool.of("my-executable"),
					new CommandLineToolArguments(ImmutableList.of("firstArg", "secondArg")))));
		}

		@Test
		void canCreateCommandLineFromNestedIterables() {
			assertThat(createSubject(singletonList(asList(asList("my-executable", "firstArg"), asList("secondArg", "thirdArg")))),
				equalTo(new DefaultCommandLine(CommandLineTool.of("my-executable"),
					new CommandLineToolArguments(ImmutableList.of("firstArg", "secondArg", "thirdArg")))));
		}

		@Test
		void canCreateCommandLineFromMultipleIterables() {
			assertThat(createSubject(asList(asList("my-executable", "firstArg"), "secondArg", singletonList("thirdArg"))),
				equalTo(new DefaultCommandLine(CommandLineTool.of("my-executable"),
					new CommandLineToolArguments(ImmutableList.of("firstArg", "secondArg", "thirdArg")))));
		}

		@Test
		void canCreateCommandLineFromCallableProvidingCommandLineSegment() {
			assertThat(createSubject(asList(callableOf(asList("my-executable", "firstArg")), callableOf("secondArg"))),
				equalTo(new DefaultCommandLine(CommandLineTool.of("my-executable"),
					new CommandLineToolArguments(ImmutableList.of("firstArg", "secondArg")))));
		}

		@Test
		void throwsExceptionOnEmptyCommandLine() {
			val ex = assertThrows(IllegalArgumentException.class, () -> createSubject(emptyList()));
			assertThat(ex.getMessage(), equalTo("The command line must contain at least one element for the executable"));
		}

		@Test
		void throwsExceptionWhenCommandLineContainsNullValues() {
			assertAll(
				() -> assertThrows(NullPointerException.class, () -> createSubject(asList("my-executable", null))),
				() -> assertThrows(NullPointerException.class, () -> createSubject(asList(null, "my-args")))
			);
		}
	}

	private static Callable<List<Object>> callableOf(List<Object> values) {
		return () -> values;
	}

	private static Callable<Object> callableOf(Object value) {
		return () -> value;
	}

	@Nested
	class OfArrayTest extends Tester {
		@Override
		CommandLine createSubject(List<?> values) {
			return CommandLine.of(values.toArray(new Object[0]));
		}

		@Test
		void throwsExceptionWhenCommandLineIsNull() {
			assertThrows(NullPointerException.class, () -> CommandLine.of((Object[]) null));
		}
	}

	@Nested
	class OfListTest extends Tester {
		@Override
		CommandLine createSubject(List<?> values) {
			return CommandLine.of(values);
		}

		@Test
		void throwsExceptionWhenCommandLineIsNull() {
			assertThrows(NullPointerException.class, () -> CommandLine.of((List<?>) null));
		}
	}
}
