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

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.matchesPattern;

class CommandLineToolExecutableTests {
	CommandLineToolExecutable subject = new CommandLineToolExecutable(Paths.get("location", "of", "executable"));

	@Test
	void hasLocation() {
		assertThat(subject.getLocation(), aFile(withAbsolutePath(endsWith("/location/of/executable"))));
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString(matchesPattern("executable 'location/of/executable'")));
	}
}
