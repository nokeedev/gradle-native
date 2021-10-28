package dev.nokee.language.base;

import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.language.base.testers.ConfigurableSourceSetIntegrationTester;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;

class ConfigurableSourceSetIntegrationTest extends ConfigurableSourceSetIntegrationTester {
	private final Project project = rootProject();
	private final ConfigurableSourceSet subject = new SourceSetFactory(project.getObjects()).sourceSet();

	@Override
	public ConfigurableSourceSet subject() {
		return subject;
	}

	@Override
	public File getTemporaryDirectory() throws IOException {
		return project.getProjectDir();
	}
}
