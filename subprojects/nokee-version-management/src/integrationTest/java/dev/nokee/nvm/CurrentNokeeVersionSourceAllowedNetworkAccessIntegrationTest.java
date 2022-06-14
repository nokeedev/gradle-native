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
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.nvm.NokeeVersion.version;
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("UnstableApiUsage")
@ExtendWith(TestDirectoryExtension.class)
class CurrentNokeeVersionSourceAllowedNetworkAccessIntegrationTest {
	@TestDirectory Path testDirectory;
	Provider<NokeeVersion> subject;

	@BeforeEach
	void setup() {
		subject = providerFactory().of(CurrentNokeeVersionSource.class, spec -> {
			spec.parameters(parameters -> {
				parameters.getNokeeVersionFile().set(testDirectory.resolve(".nokee-version").toFile());
				parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED);
				parameters.getCurrentReleaseUrl().set(testDirectory.resolve("current.json").toFile().toURI());
			});
		});
	}

	@Test
	void providesVersionFromDotNokeeVersionFile() throws IOException {
		writeCurrentVersionTo(testDirectory, "0.5.6");
		assertThat(subject, providerOf(version("0.5.6")));
	}

	@Test
	void doesNotProvideVersionWhenRemoteLocationUnavailable() {
		assertThat(subject, absentProvider());
	}

	@Test
	void doesNotProvideVersionWhenRemoteLocationMalformed() throws IOException {
		Files.write(testDirectory.resolve("current.json"), "broken!".getBytes(StandardCharsets.UTF_8));
		assertThat(subject, absentProvider());
	}
}
