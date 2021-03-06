package dev.nokee.platform.base.internal.dependencies;

import lombok.val;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_ArtifactIfExistsTest {
	@TempDir Path testDirectory;

	@Test
	void canConfigureOutgoingArtifactWithExistingFile() throws IOException {
		assertThat(testConfiguration(using(objectFactory(), artifactIfExists(existingFile()))).getOutgoing().getArtifacts().getFiles(),
			hasItem(aFileNamed(equalTo("exist.ing"))));
	}

	@Test
	void canConfigureOutgoingArtifactWithExistingDirectory() throws IOException {
		assertThat(testConfiguration(using(objectFactory(), artifactIfExists(existingDirectory()))).getOutgoing().getArtifacts().getFiles(),
			hasItem(aFileNamed(equalTo("existing"))));
	}

	@Test
	void canConfigureOutgoingArtifactWithMissingFile() {
		assertThat(testConfiguration(using(objectFactory(), artifactIfExists(missingFile()))).getOutgoing().getArtifacts().getFiles(),
			emptyIterable());
	}

	private Provider<RegularFile> existingFile() throws IOException {
		return objectFactory().fileProperty().fileValue(Files.createFile(testDirectory.resolve("exist.ing")).toFile());
	}

	private Provider<Directory> existingDirectory() throws IOException {
		return objectFactory().directoryProperty().fileValue(Files.createFile(testDirectory.resolve("existing")).toFile());
	}

	private Provider<? extends FileSystemLocation> missingFile() {
		return objectFactory().fileProperty().fileValue(testDirectory.resolve("missing").toFile());
	}

	@Test
	void alwaysThrowsExceptionWhenAsserting() throws IOException {
		val file = existingFile();
		val directory = existingDirectory();
		assertThrows(UnsupportedOperationException.class,
			() -> assertConfigured(testConfiguration(using(objectFactory(), artifactIfExists(file))), artifactIfExists(file)));
		assertThrows(UnsupportedOperationException.class,
			() -> assertConfigured(testConfiguration(using(objectFactory(), artifactIfExists(file))), artifactIfExists(directory)));
	}

	@Test
	void checkToString() throws IOException {
		assertThat(artifactIfExists(existingFile()), hasToString(startsWith("ProjectConfigurationUtils.artifactIfExists(")));
	}
}
