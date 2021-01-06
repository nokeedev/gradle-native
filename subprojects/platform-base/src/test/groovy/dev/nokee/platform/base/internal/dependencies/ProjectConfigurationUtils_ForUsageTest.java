package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ProjectConfigurationUtils.class)
class ProjectConfigurationUtils_ForUsageTest {
	@Test
	void forUsageAliasChecks() {
		assertThat(forUsage("some-usage"), equalTo(attribute(Usage.USAGE_ATTRIBUTE, named("some-usage"))));
	}
}
