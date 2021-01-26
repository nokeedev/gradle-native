package dev.gradleplugins.documentationkit;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DependencySerializerTest {
	@TempDir
	protected Path testDirectory;

	@Test
	void canDeserializeDependency() throws Exception {
		FileUtils.write(testDirectory.resolve("dependencies.xml").toFile(), "<dependencies><dependency><groupId>com.example</groupId><artifactId>foo</artifactId><version>1.0</version></dependency></dependencies>", StandardCharsets.UTF_8);

		val bob = new DependencySerializer(rootProject().getDependencies()).deserialize(testDirectory.resolve("dependencies.xml").toFile());
		assertThat(bob.get(0).getGroup(), equalTo("com.example"));
		assertThat(bob.get(0).getName(), equalTo("foo"));
		assertThat(bob.get(0).getVersion(), equalTo("1.0"));
	}


	private static Project rootProject() {
		return ProjectBuilder.builder().build();
	}
}
