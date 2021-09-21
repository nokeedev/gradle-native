/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.tasks.TaskDependency;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;

import static dev.nokee.internal.testing.ConfigurationMatchers.ofFile;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.io.FileMatchers.aFileNamed;

class ConfigurationMatchers_OfFileTest extends AbstractMatcherTest {
	@TempDir Path testDirectory;

	@Override
	protected Matcher<?> createMatcher() {
		return ofFile(aFileNamed(equalTo("foo")));
	}

	private PublishArtifact aPublishArtifact() {
		return aPublishArtifact(testDirectory.resolve("foo").toFile());
	}

	private static PublishArtifact aPublishArtifact(File file) {
		return new PublishArtifact() {
			@Override
			public String getName() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getExtension() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getType() {
				throw new UnsupportedOperationException();
			}

			@Nullable
			@Override
			public String getClassifier() {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getFile() {
				return file;
			}

			@Nullable
			@Override
			public Date getDate() {
				throw new UnsupportedOperationException();
			}

			@Override
			public TaskDependency getBuildDependencies() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Test
	void canCheckMatchingArtifactFile() {
		assertMatches(ofFile(testDirectory.resolve("foo").toFile()), aPublishArtifact(),
			"matches artifact with file named 'foo'");
	}

	@Test
	void canCheckNonMatchingArtifactFile() {
		assertDoesNotMatch(ofFile(testDirectory.resolve("bar").toFile()), aPublishArtifact(),
			"doesn't match artifact with file named 'bar'");
	}

	@Test
	void checkDescription() {
		val file = testDirectory.resolve("foo").toFile();
		assertDescription("a publish artifact with file <" + file + ">",
			ofFile(file));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("publish artifact's file was <" + testDirectory.resolve("foo") + ">",
			ofFile(testDirectory.resolve("bar").toFile()), aPublishArtifact());
	}
}
