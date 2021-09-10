package dev.nokee.model.internal;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.TestProjection;
import dev.nokee.model.core.TypeAwareModelProjection;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.NokeeExtension.nokee;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class ProjectionRealizationProvidedTest {
	private final Project project = rootProject();
	private final Action<TestProjection> a1 = Mockito.mock(Action.class);
	private final Action<Task> a2 = Mockito.mock(Action.class);
	private final InOrder inOrder = Mockito.inOrder(a1, a2);
	private TypeAwareModelProjection<Task> compileProjection;

	@BeforeEach
	void setup() {
		val container = project.getObjects().domainObjectContainer(TestProjection.class);
		nokee(project).bridgeContainer(container);

		val mainNode = nokee(project).getModelRegistry().getRoot().newChildNode("main");
		val mainProjection = mainNode.newProjection(builder -> builder.type(TestProjection.class));
		val compileNode = mainNode.newChildNode("compile");
		compileProjection = compileNode.newProjection(builder -> builder.type(Task.class));

		mainProjection.whenRealized(a1);
		compileProjection.whenRealized(a2);

		Mockito.verify(a1, never()).execute(any());
		Mockito.verify(a2, never()).execute(any());
	}

	@Test
	void realizesTaskAndItsOwnersWhenProjectionRealized() {
		compileProjection.realize();
		inOrder.verify(a1).execute(isA(TestProjection.class));
		inOrder.verify(a2).execute(isA(Task.class));
	}

	@Test
	void realizesTaskAndItsOwnersWhenTaskProviderRealized() {
		compileProjection.as(Task.class).get();
		inOrder.verify(a1).execute(isA(TestProjection.class));
		inOrder.verify(a2).execute(isA(Task.class));
	}
}
