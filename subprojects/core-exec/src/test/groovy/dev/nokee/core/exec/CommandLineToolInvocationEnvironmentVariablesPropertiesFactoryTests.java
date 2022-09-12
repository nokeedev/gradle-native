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
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineToolInvocationEnvironmentVariablesPropertiesFactoryTests {
	@Test
	void throwsExceptionForNullProperties() {
		assertThrows(NullPointerException.class, () -> from((Properties) null));
	}

	@Test
	void returnsEmptyEnvironmentVariablesForEmptyProperties() {
		assertThat(from(new Properties()), equalTo(new CommandLineToolInvocationEnvironmentVariables()));
	}

	@Test
	void returnsEnvironmentVariablesFromProperties() {
		assertThat(from(asProperties(ImmutableMap.of("K3", "v3", "K4", "v4"))),
			equalTo(new CommandLineToolInvocationEnvironmentVariables(ImmutableMap.of("K3", "v3", "K4", "v4"))));
	}

	private static Properties asProperties(Map<String, String> values) {
		val result = new Properties();
		result.putAll(values);
		return result;
	}
}
