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
package dev.nokee.nvm;

import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.nvm.NokeeVersion.version;
import static dev.nokee.nvm.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@ExtendWith({TestDirectoryExtension.class, MockitoExtension.class})
class DefaultNokeeVersionLoaderFromFileIntegrationTest {
	@TestDirectory Path testDirectory;
	@Mock NokeeVersionParser parser;
	@InjectMocks DefaultNokeeVersionLoader subject;

	@Test
	void returnsNoValueWhenVersionFileDoesNotExists() {
		assertThat(testDirectory.resolve(".nokee-version"), not(anExistingFile()));
		assertThat(subject.fromFile(testDirectory.resolve(".nokee-version")), nullValue());
	}

	@Test
	void returnsParsedVersion() throws IOException {
		when(parser.parse("1.2.3")).thenReturn(version("1.2.3"));
		assertThat(subject.fromFile(writeVersionFileTo(testDirectory, "1.2.3")), equalTo(version("1.2.3")));
	}

	@Test
	void canLoadNokeeVersionWithTailingNewline() throws IOException {
		subject.fromFile(writeVersionFileTo(testDirectory, "0.6.0\n"));
		Mockito.verify(parser).parse("0.6.0");
	}

	@Test
	void canLoadNokeeVersionWithTailingWhitespace() throws IOException {
		subject.fromFile(writeVersionFileTo(testDirectory, "1.6.0 \t "));
		Mockito.verify(parser).parse("1.6.0");
	}

	@Test
	void canLoadNokeeVersionWithPrefixWhitespace() throws IOException {
		subject.fromFile(writeVersionFileTo(testDirectory, "  \t  1.7.2"));
		Mockito.verify(parser).parse("1.7.2");
	}
}
