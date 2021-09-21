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

import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.tasks.TaskDependency;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

import static dev.nokee.internal.testing.ConfigurationMatchers.ofClassifier;

class ConfigurationMatchers_OfClassifierTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return ofClassifier("foo");
	}

	private static PublishArtifact aPublishArtifact() {
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
				return "foo";
			}

			@Override
			public File getFile() {
				throw new UnsupportedOperationException();
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
	void canCheckMatchingArtifactClassifier() {
		assertMatches(ofClassifier("foo"), aPublishArtifact(),
			"matches artifact with classifier 'foo'");
	}

	@Test
	void canCheckNonMatchingArtifactClassifier() {
		assertDoesNotMatch(ofClassifier("bar"), aPublishArtifact(),
			"doesn't match artifact with classifier 'bar'");
	}

	@Test
	void checkDescription() {
		assertDescription("a publish artifact with classifier \"directory\"",
			ofClassifier("directory"));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("publish artifact's classifier was \"foo\"",
			ofClassifier("bar"), aPublishArtifact());
	}
}
