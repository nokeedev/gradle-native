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
