package dev.nokee.model;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.ProjectMatchers.extensions;
import static dev.nokee.internal.testing.ProjectMatchers.publicType;
import static dev.nokee.model.NokeeExtension.nokee;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class ModelBasePluginTest {
	private Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply("dev.nokee.model-base");
		return project;
	}

	@Test
	void registersNokeeExtension() {
		assertThat(createSubject(), extensions(hasItem(allOf(named("nokee"), publicType(NokeeExtension.class)))));
	}

	@Test
	void finalizeProjectionsAfterProjectEvaluation() {
		val subject = createSubject();
		val action = ActionTestUtils.mockAction();
		val projection = nokee(subject).getModelRegistry().getRoot()
			.newChildNode("fjie").newProjection(builder -> builder.type(Task.class));
		projection.realizeOnFinalize().whenFinalized(action);
		assertThat(action, neverCalled());
		ProjectTestUtils.evaluate(subject);
		assertThat(action, calledOnceWith(singleArgumentOf(projection.get())));
	}
}
