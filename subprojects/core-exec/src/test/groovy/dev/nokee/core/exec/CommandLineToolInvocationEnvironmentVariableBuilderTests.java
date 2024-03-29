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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineToolInvocationEnvironmentVariableBuilderTests {
	UnpackStrategy unpackStrategy = UnpackStrategyTestUtils.mock();
	CommandLineToolInvocationEnvironmentVariables.Builder subject = new CommandLineToolInvocationEnvironmentVariables.Builder(unpackStrategy);

	@Test
	void canBuildEmptyEnvironmentVariables() {
		assertThat(subject.build(), equalTo(new CommandLineToolInvocationEnvironmentVariables()));
	}

	@Test
	void overwritePreviousEntriesUponDuplicatedKeys() {
		assertThat(subject.env("foo", "bar").envs(of("foo", "far", "bar", "dar", "far", "gar")).env("far", "zar").build(),
			equalTo(new CommandLineToolInvocationEnvironmentVariables(of("foo", "far", "bar", "dar", "far", "zar"))));
	}

	@Test
	void unpackAllValues() {
		subject.env("k1", "v1").env("k2", "v2").envs(of("k3", "v3")).build();
		Mockito.verify(unpackStrategy).unpack("v1");
		Mockito.verify(unpackStrategy).unpack("v2");
		Mockito.verify(unpackStrategy).unpack("v3");
	}

	@Test
	void checkNulls() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> subject.env(null, "val")),
			() -> assertThrows(NullPointerException.class, () -> subject.env("key", null))
		);
	}
}
