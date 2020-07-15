package dev.nokee.testing.base.internal.plugins;

import dev.nokee.testing.base.TestSuiteComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;

import javax.inject.Inject;

public abstract class TestingBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val extension = getObjects().polymorphicDomainObjectContainer(TestSuiteComponent.class);
		project.getExtensions().add(PolymorphicDomainObjectContainer.class, "testSuites", extension);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();
}
