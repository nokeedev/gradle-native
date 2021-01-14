package dev.nokee.publish.bintray.internal;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spock.lang.Subject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(BintrayPublication.class)
class BintrayPublicationTest {
	private final Project project = rootProject();
	private final Directory stagingDirectory = project.getLayout().getProjectDirectory();
	private final Path testDirectory = stagingDirectory.getAsFile().toPath();

	private BintrayPublication createSubject() {
		return new BintrayPublication(() -> stagingDirectory, () -> "1.0");
	}

	private BintrayPublication createSubject(String version) {
		return new BintrayPublication(() -> stagingDirectory, () -> version);
	}

	private BintrayPublication createSubject(Directory stagingDirectory) {
		return new BintrayPublication(() -> stagingDirectory, () -> "1.0");
	}


	private BintrayArtifact artifact(String relativePath, String version) {
		return new BintrayArtifact(testDirectory.resolve(relativePath).toFile(), relativePath, version);
	}

	private BintrayArtifact artifact(String relativePath) {
		return artifact(relativePath, "1.0");
	}

	@Test
	void canFindAllFilesRecursively() throws IOException {
		Files.createDirectories(testDirectory.resolve("foo/bar"));
		Files.createFile(testDirectory.resolve("file.txt"));
		Files.createFile(testDirectory.resolve("foo/foo.txt"));
		Files.createFile(testDirectory.resolve("foo/bar/bar.txt"));

		assertThat(createSubject().getArtifacts(), containsInAnyOrder(artifact("file.txt"), artifact("foo/foo.txt"), artifact("foo/bar/bar.txt")));
	}

	@Test
	void ignoresMavenMetadataDotXml() throws IOException {
		Files.createDirectories(testDirectory.resolve("foo/bar"));
		Files.createFile(testDirectory.resolve("foo.txt"));
		Files.createFile(testDirectory.resolve("maven-metadata.xml"));
		Files.createFile(testDirectory.resolve("foo/maven-metadata.xml"));
		Files.createFile(testDirectory.resolve("foo/bar/maven-metadata.xml"));

		assertThat(createSubject().getArtifacts(), containsInAnyOrder(artifact("foo.txt")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"maven-metadata.xml.sha256", "maven-metadata.xml.md5", "maven-metadata.xml.sha1"})
	void ignoresMavenMetadataDotXmlHashFiles(String mavenMetadataHashFilename) throws IOException {
		Files.createDirectories(testDirectory.resolve("foo/bar"));
		Files.createFile(testDirectory.resolve("bar.txt"));
		Files.createFile(testDirectory.resolve(mavenMetadataHashFilename));
		Files.createFile(testDirectory.resolve("foo/" + mavenMetadataHashFilename));
		Files.createFile(testDirectory.resolve("foo/bar/" + mavenMetadataHashFilename));

		assertThat(createSubject().getArtifacts(), containsInAnyOrder(artifact("bar.txt")));
	}

	@Test
	void includePublicationVersionInArtifactVersion() throws IOException {
		Files.createDirectories(testDirectory.resolve("foo/bar"));
		Files.createFile(testDirectory.resolve("file.txt"));
		Files.createFile(testDirectory.resolve("foo/foo.txt"));
		Files.createFile(testDirectory.resolve("foo/bar/bar.txt"));

		assertThat(createSubject("4.2").getArtifacts(),
			containsInAnyOrder(artifact("file.txt", "4.2"), artifact("foo/foo.txt", "4.2"), artifact("foo/bar/bar.txt", "4.2")));
	}

	@Test
	void doesNotThrowExceptionIfStagingDirectoryDoesNotExists() {
		val artifacts = assertDoesNotThrow(() -> createSubject(project.getLayout().getProjectDirectory().dir("missing")).getArtifacts());
		assertThat(artifacts, empty());
	}
}
