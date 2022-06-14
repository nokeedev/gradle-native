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
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.nvm.NokeeVersion.version;
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith({TestDirectoryExtension.class, MockitoExtension.class})
class DefaultNokeeVersionLoaderFromUrlIntegrationTest {
	@TestDirectory Path testDirectory;
	@Mock NokeeVersionParser parser;
	@InjectMocks DefaultNokeeVersionLoader subject;

	@Test
	void returnsNoValueWhenUrlDoesNotExists() throws IOException {
		assertThat(testDirectory.resolve("current.json"), not(anExistingFile()));
		assertThat(subject.fromUrl(testDirectory.resolve("current.json").toUri().toURL()), nullValue());
	}

	@Test
	void returnsParsedVersion() throws IOException {
		assertThat(subject.fromUrl(writeCurrentVersionTo(testDirectory, "1.2.3").toUri().toURL()),
			equalTo(version("1.2.3")));
	}

	@Test
	void returnsNoValueWhenUrlHasNoData() throws IOException {
		assertThat(subject.fromUrl(createFile(testDirectory.resolve("current.json")).toUri().toURL()), nullValue());
	}

	@Test
	void returnsNoValueWhenUrlHasMalformedData() throws IOException {
		assertThat(subject.fromUrl(write(testDirectory.resolve("current.json"), "malformed!".getBytes(UTF_8)).toUri().toURL()), nullValue());
	}
}
