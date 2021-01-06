package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.io.File;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static org.hamcrest.MatcherAssert.assertThat;

class ConsumableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ConsumableDependencyBucket> {
	@Override
	public ConsumableDependencyBucket createSubject(Project project) {
		val factory = new ConsumableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), ConfigurationDescriptionScheme.forComponent(ComponentName.of("common")));
		val bucket = create(registry(project.getObjects()), factory.create("test"));
		ModelNodes.of(bucket).realize();
		return bucket;
	}

	@Test
	void canAddProvidedArtifact() {
		val project = rootProject();
		createSubject(project).artifact(project.getLayout().getBuildDirectory().file("foo"));
		assertThat(project, hasConfiguration(hasPublishArtifact(ofFile(new File(project.getBuildDir(), "foo")))));
	}
}
