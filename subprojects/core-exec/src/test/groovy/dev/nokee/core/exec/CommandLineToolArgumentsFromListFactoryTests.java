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
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static dev.nokee.core.exec.CommandLineToolArguments.of;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CommandLineToolArgumentsFromListFactoryTests {
	@Test
	void returnsEmptyArgumentsForEmptyList() {
		assertThat(of(Collections.emptyList()), equalTo(new CommandLineToolArgumentsImpl(ImmutableList.of())));
	}

	@Test
	void returnsArgumentsFromList() {
		assertThat(of(asList("arg1", "arg2", "arg3")),
			equalTo(new CommandLineToolArgumentsImpl(ImmutableList.of("arg1", "arg2", "arg3"))));
	}
}
