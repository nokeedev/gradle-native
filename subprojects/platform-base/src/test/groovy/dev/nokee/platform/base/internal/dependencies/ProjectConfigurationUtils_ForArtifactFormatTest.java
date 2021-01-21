package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.attributes.Attribute;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ProjectConfigurationUtils.class)
class ProjectConfigurationUtils_ForArtifactFormatTest {
	@Test
	void forArtifactFormatAliasChecks() {
		assertThat(forArtifactFormat("directory"),
			equalTo(attribute(Attribute.of("artifactType", String.class), ofInstance("directory"))));
	}
}
