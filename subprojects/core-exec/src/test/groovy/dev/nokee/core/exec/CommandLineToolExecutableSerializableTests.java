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

import java.nio.file.Paths;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;

class CommandLineToolExecutableSerializableTests {
	@Test
	void canSerializeExecutableWithAbsolutePath() {
		assertThat(new CommandLineToolExecutable(Paths.get("my", "executable").toAbsolutePath()),
			isSerializable());
	}

	@Test
	void canSerializeExecutableOnly() {
		assertThat(new CommandLineToolExecutable(Paths.get("a.out")), isSerializable());
	}

	@Test
	void canSerializeExecutableWithRelativePath() {
		assertThat(new CommandLineToolExecutable(Paths.get("dir", "a.out")), isSerializable());
	}
}
