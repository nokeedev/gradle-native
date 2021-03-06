package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.attributes.DocsType;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_ForDocsTypeTest {
	@Test
	void forDocsTypeAliasChecks() {
		assertThat(forDocsType("some-doc"), equalTo(attribute(DocsType.DOCS_TYPE_ATTRIBUTE, named("some-doc"))));
	}
}
