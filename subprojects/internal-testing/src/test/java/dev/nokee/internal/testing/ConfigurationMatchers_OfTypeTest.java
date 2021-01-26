package dev.nokee.internal.testing;

import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.tasks.TaskDependency;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

import static dev.nokee.internal.testing.ConfigurationMatchers.ofType;

class ConfigurationMatchers_OfTypeTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return ofType("foo");
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
				return "foo";
			}

			@Nullable
			@Override
			public String getClassifier() {
				throw new UnsupportedOperationException();
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
		assertMatches(ofType("foo"), aPublishArtifact(),
			"matches artifact with type 'foo'");
	}

	@Test
	void canCheckNonMatchingArtifactClassifier() {
		assertDoesNotMatch(ofType("bar"), aPublishArtifact(),
			"doesn't match artifact with type 'bar'");
	}

	@Test
	void checkDescription() {
		assertDescription("a publish artifact with type \"directory\"",
			ofType("directory"));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("publish artifact's type was \"foo\"",
			ofType("bar"), aPublishArtifact());
	}
}
