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
import org.gradle.api.Action;
import org.gradle.api.provider.ValueSourceSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.nvm.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("UnstableApiUsage")
@ExtendWith(TestDirectoryExtension.class)
class NokeeVersionSourceIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void providesVersionFromDotNokeeVersionFile() throws IOException {
		writeVersionFileTo(testDirectory, "0.2.0");
		assertThat(providerFactory().of(NokeeVersionSource.class, versionFile(testDirectory.toFile())), providerOf(NokeeVersion.version("0.2.0")));
	}

	@Test
	void doesNotProvideVersionWhenDotNokeeVersionFileAbsent() {
		assertThat(providerFactory().of(NokeeVersionSource.class, versionFile(testDirectory.toFile())), absentProvider());
	}

	public static Action<ValueSourceSpec<NokeeVersionSource.Parameters>> versionFile(File baseDirectory) {
		return spec -> spec.parameters(parameters -> parameters.getNokeeVersionFile().fileValue(new File(baseDirectory, ".nokee-version")));
	}
}
