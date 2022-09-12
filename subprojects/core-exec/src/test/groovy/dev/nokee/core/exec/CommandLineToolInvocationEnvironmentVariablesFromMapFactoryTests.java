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

import java.util.Map;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineToolInvocationEnvironmentVariablesFromMapFactoryTests {
	@Test
	void throwsExceptionForNullMap() {
		assertThrows(NullPointerException.class, () -> from((Map<String, ?>) null));
	}

	@Test
	void returnsEmptyEnvironmentVariablesForEmptyMap() {
		assertThat(from(emptyMap()), equalTo(new CommandLineToolInvocationEnvironmentVariablesMapImpl()));
	}

	@Test
	void returnsEnvironmentVariablesFromMap() {
		assertThat(from(ImmutableMap.<String, Object>builder().put("K1", "v1").put("K2", "v2").build()),
			equalTo(new CommandLineToolInvocationEnvironmentVariablesMapImpl(ImmutableMap.of("K1", "v1", "K2", "v2"))));
	}
}
