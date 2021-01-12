package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.utils.TestUtils.createChildProject;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.model.internal.core.ModelNode.State.Realized;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescriptionScheme.forComponent;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@Subject(ResolvableDependencyBucketRegistrationFactory.class)
class ResolvableDependencyBucketRegistrationFactoryIntegrationTest implements DependencyBucketTester<ResolvableDependencyBucket> {
	@Override
	public ResolvableDependencyBucket createSubject(Project project) {
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project), ConfigurationNamingScheme.forComponent(ComponentName.of("common")), forComponent(ComponentName.of("common")));
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

	@Test
	void canGetAsFileCollection() {
		val rootProject = rootProject();
		val producerProject = createChildProject(rootProject, "project");
		forProject(producerProject).createIfAbsent("test", asConsumable().andThen(forUsage("bar")).andThen(it -> it.getOutgoing().artifact(producerProject.file("bar"))));

		val subjectProject = createChildProject(rootProject);
		val subject = createSubject(subjectProject);
		subject.getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, subjectProject.getObjects().named(Usage.class, "bar"));
		subject.addDependency(producerProject);
		assertThat(subject.getAsFileCollection(), contains(producerProject.file("bar")));
	}

	@Test
	void realizeNodeWhenRealized() {
		val project = rootProject();
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project));
		val bucketProvider = registry(project.getObjects()).register(factory.create("test"));
		ModelNodes.of(bucketProvider).get(Configuration.class).resolve(); // Force resolve the configuration
		assertThat(ModelNodes.of(bucketProvider).getState(), equalTo(Realized));
	}

	@Test
	void canFurtherConfigureWhenModelNodeRealized() {
		val project = rootProject();
		val factory = new ResolvableDependencyBucketRegistrationFactory(forProject(project));
		val bucketProvider = registry(project.getObjects()).register(factory.create("test"));
		bucketProvider.configure(bucket -> {
			using(project.getObjects(), forUsage(Usage.JAVA_API)).execute(bucket.getAsConfiguration());
		});
		ModelNodes.of(bucketProvider).get(Configuration.class).resolve();
		assertThat(ModelNodes.of(bucketProvider).getState(), equalTo(Realized));
	}
}
