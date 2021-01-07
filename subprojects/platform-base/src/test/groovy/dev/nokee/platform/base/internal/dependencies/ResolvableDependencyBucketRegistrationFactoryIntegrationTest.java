package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.utils.TestUtils.createChildProject;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.asConsumable;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.forUsage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@Subject(ResolvableDependencyBucketRegistrationFactory.class)
class ResolvableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ResolvableDependencyBucket> {
	@Override
	public ResolvableDependencyBucket createSubject(Project project) {
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), ConfigurationDescriptionScheme.forComponent(ComponentName.of("common")));
		val bucket = create(registry(project.getObjects()), factory.create("test"));
		ModelNodes.of(bucket).realize();
		return bucket;
	}

	@Test
	void canGetAsLenientFileCollection() {
		val rootProject = rootProject();
		val producerProjectA = createChildProject(rootProject, "projectA");
		val producerProjectB = createChildProject(rootProject, "projectB");
		forProject(producerProjectB).createIfAbsent("test", asConsumable().andThen(forUsage("foo")).andThen(it -> it.getOutgoing().artifact(producerProjectB.file("foo"))));

		val subjectProject = createChildProject(rootProject);
		val subject = createSubject(subjectProject);
		subject.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, subjectProject.getObjects().named(Usage.class, "foo"));
		subject.addDependency(producerProjectA);
		subject.addDependency(producerProjectB);
		assertThat(subject.getAsLenientFileCollection(), contains(producerProjectB.file("foo")));
	}
}
