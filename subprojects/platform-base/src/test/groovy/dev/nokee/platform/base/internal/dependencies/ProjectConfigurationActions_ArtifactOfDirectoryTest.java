package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.utils.ConfigurationTestUtils;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_ArtifactOfDirectoryTest {
	private final TaskContainer taskContainer = rootProject().getTasks();
	@TempDir Path testDirectory;

	private static Provider<Directory> aDirectory(Path directory) throws IOException {
		val a = new FileSystemWorkspace(directory);
		a.newFile("foo");
		a.newFile("a/foo");
		return directoryOf(directory);
	}

	private static Provider<Directory> directoryOf(Path directory) {
		return objectFactory().directoryProperty().fileValue(directory.toFile());
	}

	private Configuration testConfiguration() throws IOException {
		return addDirectoryArtifact(ConfigurationTestUtils.testConfiguration(), testDirectory.resolve("original"));
	}

	private Configuration testConfiguration(String name) throws IOException {
		return addDirectoryArtifact(ConfigurationTestUtils.testConfiguration(name), testDirectory.resolve("original"));
	}

	private Configuration addDirectoryArtifact(Configuration self, Path directory) throws IOException {
		using(taskContainer, artifactOf(aDirectory(directory))).execute(self);
		return self;
	}

	@Test
	void addsZipOfDirectoryAsMainPublishArtifact() throws IOException {
		assertThat(testConfiguration(), hasPublishArtifact(ofType(ZIP_TYPE)));
	}

	@Test
	void usesConfigurationNameAsClassifier() throws IOException {
		assertThat(testConfiguration("test"), hasPublishArtifact(ofClassifier("test")));
	}

	@Test
	void infersClassifierByRemovingElementsSuffixFromConfigurationName() throws IOException {
		assertThat(testConfiguration("testElements"), hasPublishArtifact(ofClassifier("test")));
	}

	@Test
	void createsZipTask() throws IOException {
		testConfiguration("testElements");
		assertThat(taskContainer, hasItem(allOf(named("zipTestElements"), isA(Zip.class))));
	}

	@Test
	void addsDirectoryAsPublishArtifactVariant() throws IOException {
		assertThat(testConfiguration(), hasOutgoingVariant(allOf(named("directory"), hasPublishArtifact(ofType(DIRECTORY_TYPE)))));
	}

	@Test
	void canAddMultipleDirectoryArtifact() throws IOException {
		assertThat(addDirectoryArtifact(testConfiguration(), testDirectory.resolve("additional")), hasOutgoingVariant(hasPublishArtifact(ofFile(aFileNamed(equalTo("additional"))))));
	}

	@Test
	void alwaysThrowsExceptionWhenAsserting() {
		assertThrows(UnsupportedOperationException.class,
			() -> assertConfigured(testConfiguration(), artifactOf(directoryOf(testDirectory))));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() throws IOException {
		val directoryA = aDirectory(testDirectory.resolve("testA"));
		val directoryB = aDirectory(testDirectory.resolve("testB"));
		new EqualsTester()
			.addEqualityGroup(artifactOf(directoryA), artifactOf(directoryA))
			.addEqualityGroup(artifactOf(directoryB))
			.testEquals();
	}

	@Test
	void checkToString() throws IOException {
		assertThat(artifactOf(aDirectory(testDirectory)),
			hasToString(startsWith("ProjectConfigurationActions.artifactOf(")));
	}
}
