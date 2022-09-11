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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CommandLineToolInvocationEnvironmentVariablesFromListFactoryTests {
	@Test
	void returnsInheritedEnvironmentVariablesForNullList() {
		assertThat(from((List<?>) null), equalTo(new CommandLineToolInvocationEnvironmentVariablesMapImpl(System.getenv())));
	}

	@Test
	void returnsEmptyEnvironmentVariablesForEmptyList() {
		assertThat(from(emptyList()), equalTo(CommandLineToolInvocationEnvironmentVariablesEmptyImpl.EMPTY_ENVIRONMENT_VARIABLES));
	}

	@Test
	void returnsEnvironmentVariablesFromList() {
		assertThat(from(asList("FOO=my-foo", "BAR=my-bar")),
			equalTo(new CommandLineToolInvocationEnvironmentVariablesMapImpl(ImmutableMap.of("FOO", "my-foo", "BAR", "my-bar"))));
	}
}
