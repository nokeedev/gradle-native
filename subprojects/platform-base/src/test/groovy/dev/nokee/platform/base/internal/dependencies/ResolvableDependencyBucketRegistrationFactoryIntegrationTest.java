package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import spock.lang.Subject;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;

@Subject(ResolvableDependencyBucketRegistrationFactory.class)
class ResolvableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ResolvableDependencyBucket> {
	@Override
	public ResolvableDependencyBucket createSubject(Project project) {
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), ConfigurationDescriptionScheme.forComponent(ComponentName.of("common")));
		val bucket = create(registry(project.getObjects()), factory.create("test"));
		ModelNodes.of(bucket).realize();
		return bucket;
	}

	// TODO: Test getAsLenientFileCollection()
}
