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

import java.nio.file.Paths;

import static dev.nokee.core.exec.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;

class CommandLineToolInvocationSerializableTests {
	@Test
	void canSerializeDefaultBuiltInvocation() {
		assertThat(new CommandLineToolInvocation.Builder().executable(new CommandLineToolExecutable(Paths.get("a.out"))).build(),
			isSerializable());
	}

	@Test
	void canSerializeInvocation() {
		assertThat(new CommandLineToolInvocation(new CommandLineToolExecutable(Paths.get("a.out")), new CommandLineToolArguments(ImmutableList.of("arg1", "arg2")), new CommandLineToolInvocationOutputRedirection.ToSystemOutputRedirection(), new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection(), Paths.get("working", "directory"), new CommandLineToolInvocationEnvironmentVariables(ImmutableMap.of("k1", "v1", "k2", "v2"))),
			isSerializable());
	}
}
